package com.enodeframework.domain;

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
     * Update the given aggregate root's memory cache.
     *
     * @param aggregateRoot
     * @return
     */
    CompletableFuture<Void> updateAggregateRootCache(IAggregateRoot aggregateRoot);

    /**
     * Refresh the aggregate memory cache by replaying events of event store, and return the refreshed aggregate root.
     *
     * @param aggregateRootTypeName
     * @param aggregateRootId
     */
    CompletableFuture<IAggregateRoot> refreshAggregateFromEventStoreAsync(String aggregateRootTypeName, Object aggregateRootId);

    /**
     * Refresh the aggregate memory cache by replaying events of event store, and return the refreshed aggregate root.
     *
     * @param aggregateRootType
     * @param aggregateRootId
     */
    <T extends IAggregateRoot> CompletableFuture<T> refreshAggregateFromEventStoreAsync(Class<T> aggregateRootType, Object aggregateRootId);

    /**
     * Start background tasks.
     */
    void start();

    /**
     * Stop background tasks.
     */
    void stop();
}
