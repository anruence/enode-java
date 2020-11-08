package org.enodeframework.domain;

import java.util.concurrent.CompletableFuture;

public interface IMemoryCache {
    /**
     * Get an aggregate from memory cache.
     */
    CompletableFuture<IAggregateRoot> getAsync(Object aggregateRootId);

    /**
     * Get a strong type aggregate from memory cache.
     */
    <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object aggregateRootId, Class<T> aggregateRootType);

    /**
     * Accept the given aggregate root's changes.
     */
    CompletableFuture<Boolean> acceptAggregateRootChanges(IAggregateRoot aggregateRoot);

    /**
     * Refresh the aggregate memory cache by replaying events of event store, and return the refreshed aggregate root.
     */
    CompletableFuture<IAggregateRoot> refreshAggregateFromEventStoreAsync(String aggregateRootTypeName, String aggregateRootId);

    /**
     * Refresh the aggregate memory cache by replaying events of event store, and return the refreshed aggregate root.
     */
    <T extends IAggregateRoot> CompletableFuture<T> refreshAggregateFromEventStoreAsync(Class<T> aggregateRootType, String aggregateRootId);

    /**
     * Start background tasks.
     */
    void start();

    /**
     * Stop background tasks.
     */
    void stop();
}
