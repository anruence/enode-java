package com.enodeframework.domain.impl;

import com.enodeframework.common.io.Task;
import com.enodeframework.domain.IAggregateRepositoryProvider;
import com.enodeframework.domain.IAggregateRepositoryProxy;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.domain.IAggregateSnapshotter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DefaultAggregateSnapshotter implements IAggregateSnapshotter {

    @Autowired
    private IAggregateRepositoryProvider aggregateRepositoryProvider;

    public DefaultAggregateSnapshotter setAggregateRepositoryProvider(IAggregateRepositoryProvider aggregateRepositoryProvider) {
        this.aggregateRepositoryProvider = aggregateRepositoryProvider;
        return this;
    }

    @Override
    public CompletableFuture<IAggregateRoot> restoreFromSnapshotAsync(Class aggregateRootType, String aggregateRootId) {
        IAggregateRepositoryProxy aggregateRepository = aggregateRepositoryProvider.getRepository(aggregateRootType);
        if (aggregateRepository == null) {
            return Task.completedFuture(null);
        }
        return aggregateRepository.getAsync(aggregateRootId);
    }
}
