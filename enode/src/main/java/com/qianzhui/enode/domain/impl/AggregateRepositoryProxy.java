package com.qianzhui.enode.domain.impl;

import com.qianzhui.enode.domain.IAggregateRepository;
import com.qianzhui.enode.domain.IAggregateRepositoryProxy;
import com.qianzhui.enode.domain.IAggregateRoot;

import java.util.concurrent.CompletableFuture;

public class AggregateRepositoryProxy<TAggregateRoot extends IAggregateRoot> implements IAggregateRepositoryProxy {
    private final IAggregateRepository<TAggregateRoot> _aggregateRepository;

    public AggregateRepositoryProxy(IAggregateRepository<TAggregateRoot> aggregateRepository) {
        _aggregateRepository = aggregateRepository;
    }

    @Override
    public Object getInnerObject() {
        return _aggregateRepository;
    }

    @Override
    public CompletableFuture<IAggregateRoot> getAsync(String aggregateRootId) {
        return (CompletableFuture<IAggregateRoot>) _aggregateRepository.getAsync(aggregateRootId);
    }
}
