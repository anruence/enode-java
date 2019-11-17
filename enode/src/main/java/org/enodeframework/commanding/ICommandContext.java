package org.enodeframework.commanding;

import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.messaging.IApplicationMessage;

import java.util.concurrent.CompletableFuture;

public interface ICommandContext {
    /**
     * Add a new aggregate into the current command context.
     */
    void add(IAggregateRoot aggregateRoot);

    /**
     * Add a new aggregate into the current command context synchronously, and then return a completed task object.
     */
    CompletableFuture<Void> addAsync(IAggregateRoot aggregateRoot);

    /**
     * Get an aggregate from the current command context.
     */
    <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object id, boolean firstFromCache, Class<T> clazz);

    <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object id, Class<T> clazz);

    String getResult();

    void setResult(String result);

    /**
     * Get an application message.
     */
    IApplicationMessage getApplicationMessage();

    /**
     * Set an application message.
     */
    void setApplicationMessage(IApplicationMessage applicationMessage);

}
