package com.enodeframework.domain.impl;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.Task;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.domain.IAggregateRootFactory;
import com.enodeframework.domain.IAggregateSnapshotter;
import com.enodeframework.domain.IAggregateStorage;
import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.IEventStore;
import com.enodeframework.infrastructure.ITypeNameProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class EventSourcingAggregateStorage implements IAggregateStorage {

    private static final int MINVERSION = 1;

    private static final int MAXVERSION = Integer.MAX_VALUE;

    @Autowired
    private IAggregateRootFactory aggregateRootFactory;

    @Autowired
    private IEventStore eventStore;

    @Autowired
    private IAggregateSnapshotter aggregateSnapshotter;

    @Autowired
    private ITypeNameProvider typeNameProvider;

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, String aggregateRootId) {
        if (aggregateRootType == null) {
            throw new NullPointerException("aggregateRootType");
        }
        if (aggregateRootId == null) {
            throw new NullPointerException("aggregateRootId");
        }
        CompletableFuture<T> aggregateRootFuture = tryGetFromSnapshot(aggregateRootId, aggregateRootType);
        return aggregateRootFuture.thenCompose(aggregateRoot -> {
            if (aggregateRoot != null) {
                return CompletableFuture.completedFuture(aggregateRoot);
            }
            String aggregateRootTypeName = typeNameProvider.getTypeName(aggregateRootType);
            CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> eventStreamsFuture = eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, MINVERSION, MAXVERSION);
            return eventStreamsFuture.thenApply(eventStreams -> {
                List<DomainEventStream> domainEventStreams = eventStreams.getData();
                T reAggregateRoot = rebuildAggregateRoot(aggregateRootType, domainEventStreams);
                return reAggregateRoot;
            });
        });
    }

    private <T extends IAggregateRoot> CompletableFuture<T> tryGetFromSnapshot(String aggregateRootId, Class<T> aggregateRootType) {
        CompletableFuture<T> aggregateRootFuture = aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId);
        CompletableFuture<T> ret = aggregateRootFuture.thenCompose((aggregateRoot) -> {
            if (aggregateRoot == null) {
                return Task.CompletedTask;
            }
            if (aggregateRoot.getClass() != aggregateRootType || !aggregateRoot.uniqueId().equals(aggregateRootId)) {
                throw new RuntimeException(String.format("AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType:%s,aggregateRootId:%s], expected: [aggregateRootType:%s,aggregateRootId:%s]",
                        aggregateRoot.getClass(),
                        aggregateRoot.uniqueId(),
                        aggregateRootType,
                        aggregateRootId));
            }
            String aggregateRootTypeName = typeNameProvider.getTypeName(aggregateRootType);
            CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> eventStreamsFuture = eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, aggregateRoot.version() + 1, MAXVERSION);
            return eventStreamsFuture.thenApply(eventStreams -> {
                List<DomainEventStream> eventStreamsAfterSnapshot = eventStreams.getData();
                aggregateRoot.replayEvents(eventStreamsAfterSnapshot);
                return aggregateRoot;
            });
        });
        return ret;
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
