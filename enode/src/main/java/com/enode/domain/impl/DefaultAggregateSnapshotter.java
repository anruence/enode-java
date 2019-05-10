package com.enode.domain.impl;

import com.enode.domain.IAggregateRepositoryProvider;
import com.enode.domain.IAggregateRepositoryProxy;
import com.enode.domain.IAggregateRoot;
import com.enode.domain.IAggregateSnapshotter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class DefaultAggregateSnapshotter implements IAggregateSnapshotter {

    @Autowired
    private IAggregateRepositoryProvider _aggregateRepositoryProvider;

    @Override
    public CompletableFuture<IAggregateRoot> restoreFromSnapshotAsync(Class aggregateRootType, String aggregateRootId) {
        IAggregateRepositoryProxy aggregateRepository = _aggregateRepositoryProvider.getRepository(aggregateRootType);
        if (aggregateRepository == null) {
            return CompletableFuture.completedFuture(null);
        }
        return aggregateRepository.getAsync(aggregateRootId);
    }
}
