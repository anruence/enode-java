package org.enodeframework.domain.impl;

import org.enodeframework.domain.IAggregateRepositoryProvider;
import org.enodeframework.domain.IAggregateRepositoryProxy;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IAggregateSnapshotter;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DefaultAggregateSnapshotter implements IAggregateSnapshotter {

    private final IAggregateRepositoryProvider aggregateRepositoryProvider;

    public DefaultAggregateSnapshotter(IAggregateRepositoryProvider aggregateRepositoryProvider) {
        this.aggregateRepositoryProvider = aggregateRepositoryProvider;
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> restoreFromSnapshotAsync(Class<T> aggregateRootType, String aggregateRootId) {
        CompletableFuture<T> future = new CompletableFuture<>();
        IAggregateRepositoryProxy aggregateRepository = aggregateRepositoryProvider.getRepository(aggregateRootType);
        if (aggregateRepository == null) {
            future.complete(null);
            return future;
        }
        return aggregateRepository.getAsync(aggregateRootId);
    }
}
