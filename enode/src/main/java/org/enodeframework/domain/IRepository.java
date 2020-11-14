package org.enodeframework.domain;

import java.util.concurrent.CompletableFuture;

public interface IRepository {
    /**
     * Get an aggregate from memory cache, if not exist, get it from event store.
     */
    <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, Object aggregateRootId);

    /**
     * Get an aggregate from memory cache, if not exist, get it from event store.
     */
    CompletableFuture<IAggregateRoot> getAsync(Object aggregateRootId);

    /**
     * Refresh memory cache with given aggregate.
     */
    <T extends IAggregateRoot> void refreshAggregate(T aggregateRoot);
}
