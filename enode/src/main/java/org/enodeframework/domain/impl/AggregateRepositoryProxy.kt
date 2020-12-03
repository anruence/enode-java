package org.enodeframework.domain.impl

import org.enodeframework.domain.IAggregateRepository
import org.enodeframework.domain.IAggregateRepositoryProxy
import org.enodeframework.domain.IAggregateRoot
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class AggregateRepositoryProxy<TAggregateRoot : IAggregateRoot?> : IAggregateRepositoryProxy {
    private lateinit var aggregateRepository: IAggregateRepository<TAggregateRoot>

    override fun getInnerObject(): Any {
        return aggregateRepository
    }

    override fun setInnerObject(innerObject: Any) {
        aggregateRepository = innerObject as IAggregateRepository<TAggregateRoot>
    }

    override fun <T : IAggregateRoot?> getAsync(aggregateRootId: String): CompletableFuture<T> {
        return aggregateRepository.getAsync(aggregateRootId) as CompletableFuture<T>
    }
}