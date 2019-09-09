package org.enodeframework.domain.impl;

import org.enodeframework.domain.IAggregateRepository;
import org.enodeframework.domain.IAggregateRepositoryProxy;
import org.enodeframework.domain.IAggregateRoot;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class AggregateRepositoryProxy<TAggregateRoot extends IAggregateRoot> implements IAggregateRepositoryProxy {
    private final IAggregateRepository<TAggregateRoot> aggregateRepository;

    public AggregateRepositoryProxy(IAggregateRepository<TAggregateRoot> aggregateRepository) {
        this.aggregateRepository = aggregateRepository;
    }

    @Override
    public Object getInnerObject() {
        return aggregateRepository;
    }

    @Override
    public CompletableFuture<IAggregateRoot> getAsync(String aggregateRootId) {
        return (CompletableFuture<IAggregateRoot>) aggregateRepository.getAsync(aggregateRootId);
    }
}
