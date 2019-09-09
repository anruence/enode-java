package org.enodeframework.commanding;

import org.enodeframework.domain.IAggregateRoot;

import java.util.concurrent.CompletableFuture;

public interface ICommandContext {
    /**
     * Add a new aggregate into the current command context.
     *
     * @param aggregateRoot
     */
    void add(IAggregateRoot aggregateRoot);

    /**
     * Add a new aggregate into the current command context synchronously, and then return a completed task object.
     *
     * @param aggregateRoot
     * @return
     */
    CompletableFuture<Void> addAsync(IAggregateRoot aggregateRoot);

    /**
     * Get an aggregate from the current command context.
     *
     * @param <T>
     * @param id
     * @param firstFromCache
     * @return
     */
    <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object id, boolean firstFromCache, Class<T> clazz);

    <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object id, Class<T> clazz);

    String getResult();

    void setResult(String result);
}
