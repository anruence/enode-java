package com.enode.domain;

import java.util.concurrent.CompletableFuture;

public interface IMemoryCache {
    /**
     * Get an aggregate from memory cache.
     *
     * @param aggregateRootId
     * @return
     */
    CompletableFuture<IAggregateRoot> getAsync(Object aggregateRootId);

    /**
     * Get a strong type aggregate from memory cache.
     *
     * @param aggregateRootId
     * @param aggregateRootType
     * @param <T>
     * @return
     */
    <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object aggregateRootId, Class<T> aggregateRootType);

    /**
     * Set an aggregate to memory cache.
     *
     * @param aggregateRoot
     */
    void set(IAggregateRoot aggregateRoot);

    /**
     * Refresh the aggregate memory cache by replaying events of event store.
     *
     * @param aggregateRootTypeName
     * @param aggregateRootId
     */
    CompletableFuture refreshAggregateFromEventStoreAsync(String aggregateRootTypeName, String aggregateRootId);

    /**
     * Start background tasks.
     */
    void start();

    /**
     * Stop background tasks.
     */
    void stop();
}
