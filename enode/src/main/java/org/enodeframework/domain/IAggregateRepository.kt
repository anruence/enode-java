package org.enodeframework.domain

import java.util.concurrent.CompletableFuture

interface IAggregateRepository<T : IAggregateRoot?> {
    fun getAsync(aggregateRootId: String): CompletableFuture<T>
}