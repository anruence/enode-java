package com.enode.domain;

import java.util.concurrent.CompletableFuture;

public interface IRepository {
    /**
     * Get an aggregate from memory cache, if not exist, get it from event store.
     *
     * @param aggregateRootType
     * @param aggregateRootId
     * @param <T>
     * @return
     */
    <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, Object aggregateRootId);

    /**
     * Get an aggregate from memory cache, if not exist, get it from event store.
     *
     * @param aggregateRootId
     * @return
     */
    CompletableFuture<IAggregateRoot> getAsync(Object aggregateRootId);
}
