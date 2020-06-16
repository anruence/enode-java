package org.enodeframework.domain.impl;

import org.enodeframework.common.exception.EnodeRuntimeException;
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
        CompletableFuture<T> future = new CompletableFuture<>();
        if (aggregateRootId == null) {
            future.completeExceptionally(new IllegalArgumentException("aggregateRootId"));
            return future;
        }
        if (aggregateRootType == null) {
            future.completeExceptionally(new IllegalArgumentException("aggregateRootType"));
            return future;
        }
        return tryGetFromSnapshot(aggregateRootId, aggregateRootType).thenCompose(aggregateRoot -> {
            if (aggregateRoot != null) {
                return CompletableFuture.completedFuture(aggregateRoot);
            }
            String aggregateRootTypeName = typeNameProvider.getTypeName(aggregateRootType);
            return eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, MINVERSION, MAXVERSION)
                    .thenApply(eventStreams -> rebuildAggregateRoot(aggregateRootType, eventStreams));
        });
    }

    private <T extends IAggregateRoot> CompletableFuture<T> tryGetFromSnapshot(String aggregateRootId, Class<T> aggregateRootType) {
        CompletableFuture<T> aggregateRootFuture = aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId);
        return aggregateRootFuture.thenCompose((aggregateRoot) -> {
            if (aggregateRoot == null) {
                return CompletableFuture.completedFuture(null);
            }
            if (aggregateRoot.getClass() != aggregateRootType || !aggregateRoot.getUniqueId().equals(aggregateRootId)) {
                throw new EnodeRuntimeException(String.format("AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType:%s,aggregateRootId:%s], expected: [aggregateRootType:%s,aggregateRootId:%s]",
                        aggregateRoot.getClass(),
                        aggregateRoot.getUniqueId(),
                        aggregateRootType,
                        aggregateRootId));
            }
            String aggregateRootTypeName = typeNameProvider.getTypeName(aggregateRootType);
            CompletableFuture<List<DomainEventStream>> eventStreamsFuture = eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, aggregateRoot.getVersion() + 1, MAXVERSION);
            return eventStreamsFuture.thenApply(eventStreams -> {
                List<DomainEventStream> eventStreamsAfterSnapshot = eventStreams;
                aggregateRoot.replayEvents(eventStreamsAfterSnapshot);
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
