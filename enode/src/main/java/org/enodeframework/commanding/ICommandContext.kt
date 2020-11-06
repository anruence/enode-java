package org.enodeframework.commanding

import org.enodeframework.domain.IAggregateRoot
import org.enodeframework.messaging.IApplicationMessage
import java.util.concurrent.CompletableFuture

interface ICommandContext {
    /**
     * Add a new aggregate into the current command context.
     */
    fun add(aggregateRoot: IAggregateRoot?)

    /**
     * Add a new aggregate into the current command context synchronously, and then return a completed task object.
     */
    fun addAsync(aggregateRoot: IAggregateRoot?): CompletableFuture<Void?>?

    /**
     * Get an aggregate from the current command context.
     */
    fun <T : IAggregateRoot?> getAsync(id: Any?, firstFromCache: Boolean, clazz: Class<T>?): CompletableFuture<T>?
    fun <T : IAggregateRoot?> getAsync(id: Any?, clazz: Class<T>?): CompletableFuture<T>?
    var result: String?
    /**
     * Get an application message.
     */
    /**
     * Set an application message.
     */
    var applicationMessage: IApplicationMessage?
}