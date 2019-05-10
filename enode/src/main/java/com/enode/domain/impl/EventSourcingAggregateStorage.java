package com.enode.domain.impl;

import com.enode.common.io.AsyncTaskResult;
import com.enode.domain.IAggregateRoot;
import com.enode.domain.IAggregateRootFactory;
import com.enode.domain.IAggregateSnapshotter;
import com.enode.domain.IAggregateStorage;
import com.enode.eventing.DomainEventStream;
import com.enode.eventing.IEventStore;
import com.enode.infrastructure.ITypeNameProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventSourcingAggregateStorage implements IAggregateStorage {

    private static final int MIN_VERSION = 1;

    private static final int MAX_VERSION = Integer.MAX_VALUE;

    @Autowired
    private IAggregateRootFactory _aggregateRootFactory;

    @Autowired
    private IEventStore _eventStore;

    @Autowired
    private IAggregateSnapshotter _aggregateSnapshotter;

    @Autowired
    private ITypeNameProvider _typeNameProvider;

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, String aggregateRootId) {
        if (aggregateRootType == null) {
            throw new NullPointerException("aggregateRootType");
        }
        if (aggregateRootId == null) {
            throw new NullPointerException("aggregateRootId");
        }

        CompletableFuture<T> aggregateRootFuture = tryGetFromSnapshot(aggregateRootId, aggregateRootType);

        // 使用
        CompletableFuture<T> ret = aggregateRootFuture.thenCompose(aggregateRoot -> {
            if (aggregateRoot != null) {
                return CompletableFuture.completedFuture(aggregateRoot);
            }
            String aggregateRootTypeName = _typeNameProvider.getTypeName(aggregateRootType);
            CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> eventStreamsFuture = _eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, MIN_VERSION, Integer.MAX_VALUE);
            return eventStreamsFuture.thenApply(eventStreams -> {
                List<DomainEventStream> domainEventStreams = eventStreams.getData();
                T reAggregateRoot = rebuildAggregateRoot(aggregateRootType, domainEventStreams);
                return reAggregateRoot;
            });
        });
        return ret;
    }

    private <T extends IAggregateRoot> CompletableFuture<T> tryGetFromSnapshot(String aggregateRootId, Class<T> aggregateRootType) {
        CompletableFuture<T> aggregateRootFuture = _aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId);
        CompletableFuture<T> ret = aggregateRootFuture.thenCompose((aggregateRoot) -> {
            if (aggregateRoot == null) {
                return CompletableFuture.completedFuture(null);
            }
            if (aggregateRoot.getClass() != aggregateRootType || !aggregateRoot.uniqueId().equals(aggregateRootId)) {
                throw new RuntimeException(String.format("AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType:%s,aggregateRootId:%s], expected: [aggregateRootType:%s,aggregateRootId:%s]",
                        aggregateRoot.getClass(),
                        aggregateRoot.uniqueId(),
                        aggregateRootType,
                        aggregateRootId));
            }
            String aggregateRootTypeName = _typeNameProvider.getTypeName(aggregateRootType);
            CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> eventStreamsFuture = _eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, aggregateRoot.version() + 1, Integer.MAX_VALUE);
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
        T aggregateRoot = _aggregateRootFactory.createAggregateRoot(aggregateRootType);
        aggregateRoot.replayEvents(eventStreams);
        return aggregateRoot;
    }
}
