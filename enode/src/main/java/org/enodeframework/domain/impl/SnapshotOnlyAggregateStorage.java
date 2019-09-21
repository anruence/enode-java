package org.enodeframework.domain.impl;

import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IAggregateSnapshotter;
import org.enodeframework.domain.IAggregateStorage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class SnapshotOnlyAggregateStorage implements IAggregateStorage {
    @Autowired
    private IAggregateSnapshotter aggregateSnapshotter;

    public SnapshotOnlyAggregateStorage setAggregateSnapshotter(IAggregateSnapshotter aggregateSnapshotter) {
        this.aggregateSnapshotter = aggregateSnapshotter;
        return this;
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, String aggregateRootId) {
        if (aggregateRootType == null) {
            throw new IllegalArgumentException("aggregateRootType");
        }
        if (aggregateRootId == null) {
            throw new IllegalArgumentException("aggregateRootId");
        }
        CompletableFuture<T> future = aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId);
        return future.thenApply(aggregateRoot -> {
            if (aggregateRoot != null && (aggregateRoot.getClass() != aggregateRootType || !aggregateRoot.getUniqueId().equals(aggregateRootId))) {
                throw new RuntimeException(String.format("AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType:%s,aggregateRootId:%s], expected: [aggregateRootType:%s,aggregateRootId:%s]",
                        aggregateRoot.getClass(),
                        aggregateRoot.getUniqueId(),
                        aggregateRootType,
                        aggregateRootId));
            }
            return aggregateRoot;
        });
    }
}
