package org.enodeframework.domain.impl;

import org.enodeframework.common.exception.EnodeRuntimeException;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IAggregateSnapshotter;
import org.enodeframework.domain.IAggregateStorage;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class SnapshotOnlyAggregateStorage implements IAggregateStorage {

    private final IAggregateSnapshotter aggregateSnapshotter;

    public SnapshotOnlyAggregateStorage(IAggregateSnapshotter aggregateSnapshotter) {
        this.aggregateSnapshotter = aggregateSnapshotter;
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
        return aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId)
                .thenApply(aggregateRoot -> {
                    if (aggregateRoot != null && (aggregateRoot.getClass() != aggregateRootType || !aggregateRoot.getUniqueId().equals(aggregateRootId))) {
                        throw new EnodeRuntimeException(String.format("AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType:%s,aggregateRootId:%s], expected: [aggregateRootType:%s,aggregateRootId:%s]",
                                aggregateRoot.getClass(),
                                aggregateRoot.getUniqueId(),
                                aggregateRootType,
                                aggregateRootId));
                    }
                    return aggregateRoot;
                });
    }
}
