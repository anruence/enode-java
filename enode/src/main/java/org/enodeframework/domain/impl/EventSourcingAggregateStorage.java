package org.enodeframework.domain.impl;

import org.enodeframework.common.exception.AggregateRootInvalidException;
import org.enodeframework.common.exception.AggregateRootNotFoundException;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IAggregateRootFactory;
import org.enodeframework.domain.IAggregateSnapshotter;
import org.enodeframework.domain.IAggregateStorage;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.IEventStore;
import org.enodeframework.infrastructure.ITypeNameProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class EventSourcingAggregateStorage implements IAggregateStorage {

    private static final int MINVERSION = 1;

    private static final int MAXVERSION = Integer.MAX_VALUE;
    private final IAggregateRootFactory aggregateRootFactory;
    private final IEventStore eventStore;
    private final IAggregateSnapshotter aggregateSnapshotter;
    private final ITypeNameProvider typeNameProvider;

    public EventSourcingAggregateStorage(IEventStore eventStore, IAggregateRootFactory aggregateRootFactory, IAggregateSnapshotter aggregateSnapshotter, ITypeNameProvider typeNameProvider) {
        this.aggregateRootFactory = aggregateRootFactory;
        this.eventStore = eventStore;
        this.aggregateSnapshotter = aggregateSnapshotter;
        this.typeNameProvider = typeNameProvider;
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, String aggregateRootId) {
        Ensure.notNull(aggregateRootId, "aggregateRootId");
        Ensure.notNull(aggregateRootType, "aggregateRootType");
        return tryGetFromSnapshot(aggregateRootId, aggregateRootType).thenCompose(aggregateRoot -> {
            if (aggregateRoot != null) {
                return CompletableFuture.completedFuture(aggregateRoot);
            }
            String aggregateRootTypeName = typeNameProvider.getTypeName(aggregateRootType);
            return tryQueryAggregateEventsAsync(aggregateRootType, aggregateRootTypeName, aggregateRootId, MINVERSION, MAXVERSION, 0, new CompletableFuture<>()).thenApply(eventStreams -> {
                T rebuild = rebuildAggregateRoot(aggregateRootType, eventStreams);
                if (rebuild == null) {
                    throw new AggregateRootNotFoundException(String.format("aggregateRoot not found, [aggregateRootType: %s, aggregateRootId: %s]", aggregateRootType.getName(), aggregateRootId));
                }
                return rebuild;
            });
        });
    }

    private <T extends IAggregateRoot> CompletableFuture<T> tryRestoreFromSnapshotAsync(Class<T> aggregateRootType, String aggregateRootId, int retryTimes, CompletableFuture<T> taskSource) {
        IOHelper.tryAsyncActionRecursively("TryRestoreFromSnapshotAsync",
                () -> aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId),
                taskSource::complete,
                () -> String.format("tryRestoreFromSnapshotAsync has unknown exception, aggregateRootType: %s, aggregateRootId: %s", aggregateRootType.getName(), aggregateRootId),
                null,
                retryTimes, true);
        return taskSource;
    }

    private CompletableFuture<List<DomainEventStream>> tryQueryAggregateEventsAsync(Class<?> aggregateRootType, String aggregateRootTypeName, String aggregateRootId, int minVersion, int maxVersion, int retryTimes, CompletableFuture<List<DomainEventStream>> taskSource) {
        IOHelper.tryAsyncActionRecursively("TryQueryAggregateEventsAsync",
                () -> eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, minVersion, maxVersion),
                taskSource::complete,
                () -> String.format("eventStore.queryAggregateEventsAsync has unknown exception, aggregateRootTypeName: %s, aggregateRootId: %s", aggregateRootTypeName, aggregateRootId),
                null,
                retryTimes, true);
        return taskSource;
    }

    private <T extends IAggregateRoot> CompletableFuture<T> tryGetFromSnapshot(String aggregateRootId, Class<T> aggregateRootType) {
        CompletableFuture<T> aggregateRootFuture = tryRestoreFromSnapshotAsync(aggregateRootType, aggregateRootId, 0, new CompletableFuture<>());
        return aggregateRootFuture.thenCompose((aggregateRoot) -> {
            if (aggregateRoot == null) {
                return CompletableFuture.completedFuture(null);
            }
            if (aggregateRoot.getClass() != aggregateRootType || !aggregateRoot.getUniqueId().equals(aggregateRootId)) {
                throw new AggregateRootInvalidException(String.format("AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType: %s, aggregateRootId: %s], expected: [aggregateRootType: %s, aggregateRootId: %s]",
                        aggregateRoot.getClass(),
                        aggregateRoot.getUniqueId(),
                        aggregateRootType,
                        aggregateRootId));
            }
            String aggregateRootTypeName = typeNameProvider.getTypeName(aggregateRootType);
            CompletableFuture<List<DomainEventStream>> eventStreamsFuture = tryQueryAggregateEventsAsync(aggregateRootType, aggregateRootTypeName, aggregateRootId, aggregateRoot.getVersion() + 1, MAXVERSION, 0, new CompletableFuture<>());
            return eventStreamsFuture.thenApply(eventStreams -> {
                aggregateRoot.replayEvents(eventStreams);
                return aggregateRoot;
            });
        });
    }

    private <T extends IAggregateRoot> T rebuildAggregateRoot(Class<T> aggregateRootType, List<DomainEventStream> eventStreams) {
        if (eventStreams == null || eventStreams.isEmpty()) {
            return null;
        }
        T aggregateRoot = aggregateRootFactory.createAggregateRoot(aggregateRootType);
        aggregateRoot.replayEvents(eventStreams);
        return aggregateRoot;
    }
}
