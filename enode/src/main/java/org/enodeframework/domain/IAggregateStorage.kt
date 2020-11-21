package org.enodeframework.domain

import java.util.concurrent.CompletableFuture

/**
 * Represents an aggregate storage interface.
 */
interface IAggregateStorage {
    /**
     * Get an aggregate from aggregate storage.
     */
    fun <T : IAggregateRoot?> getAsync(aggregateRootType: Class<T>, aggregateRootId: String): CompletableFuture<T>
}