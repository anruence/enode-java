package org.enodeframework.domain.impl;

import org.enodeframework.domain.IAggregateRepository;
import org.enodeframework.domain.IAggregateRepositoryProxy;
import org.enodeframework.domain.IAggregateRoot;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class AggregateRepositoryProxy<TAggregateRoot extends IAggregateRoot> implements IAggregateRepositoryProxy {
    private IAggregateRepository<TAggregateRoot> aggregateRepository;

    @Override
    public Object getInnerObject() {
        return aggregateRepository;
    }

    @Override
    public void setInnerObject(Object innerObject) {
        this.aggregateRepository = (IAggregateRepository<TAggregateRoot>) innerObject;
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(String aggregateRootId) {
        return (CompletableFuture<T>) aggregateRepository.getAsync(aggregateRootId);
    }
}
