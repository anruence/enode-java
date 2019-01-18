package com.enode.domain;

import java.util.concurrent.CompletableFuture;

/**
 * An interface which can restore aggregate from snapshot storage.
 */
public interface IAggregateSnapshotter {
    /**
     * Restore the aggregate from snapshot storage.
     *
     * @param aggregateRootType
     * @param aggregateRootId
     * @param <T>
     * @return
     */
    <T extends IAggregateRoot> CompletableFuture<T> restoreFromSnapshotAsync(Class<T> aggregateRootType, String aggregateRootId);
}
