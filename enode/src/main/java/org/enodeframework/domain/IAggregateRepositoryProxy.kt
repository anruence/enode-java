package org.enodeframework.domain

import org.enodeframework.infrastructure.IObjectProxy
import java.util.concurrent.CompletableFuture

interface IAggregateRepositoryProxy : IObjectProxy {
    fun <T : IAggregateRoot?> getAsync(aggregateRootId: String): CompletableFuture<T>
}