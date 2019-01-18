package com.enode.domain.impl;

import com.enode.domain.IAggregateRoot;
import com.enode.domain.IAggregateSnapshotter;
import com.enode.domain.IAggregateStorage;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class SnapshotOnlyAggregateStorage implements IAggregateStorage {
    private final IAggregateSnapshotter _aggregateSnapshotter;

    @Inject
    public SnapshotOnlyAggregateStorage(IAggregateSnapshotter aggregateSnapshotter) {
        _aggregateSnapshotter = aggregateSnapshotter;
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, String aggregateRootId) {
        if (aggregateRootType == null) {
            throw new NullPointerException("aggregateRootType");
        }
        if (aggregateRootId == null) {
            throw new NullPointerException("aggregateRootId");
        }
        CompletableFuture<T> future = _aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId);
        return future.thenApply(aggregateRoot -> {
            if (aggregateRoot != null && (aggregateRoot.getClass() != aggregateRootType || !aggregateRoot.uniqueId().equals(aggregateRootId))) {
                throw new RuntimeException(String.format("AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType:%s,aggregateRootId:%s], expected: [aggregateRootType:%s,aggregateRootId:%s]",
                        aggregateRoot.getClass(),
                        aggregateRoot.uniqueId(),
                        aggregateRootType,
                        aggregateRootId));
            }
            return aggregateRoot;
        });
    }
}
