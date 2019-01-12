package com.qianzhui.enode.commanding;

import com.qianzhui.enode.domain.IAggregateRoot;

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
    CompletableFuture addAsync(IAggregateRoot aggregateRoot);

    /**
     * Get an aggregate from the current command context.
     *
     * @param <T>
     * @param id
     * @param firstFromCache
     * @return
     */
    <T extends IAggregateRoot> CompletableFuture getAsync(Object id, boolean firstFromCache, Class<T> clazz);

    void setResult(String result);

    String getResult();
}
