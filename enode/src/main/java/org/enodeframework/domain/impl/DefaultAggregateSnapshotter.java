package org.enodeframework.domain.impl;

import org.enodeframework.common.io.IOHelper;
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
        IAggregateRepositoryProxy aggregateRepository = aggregateRepositoryProvider.getRepository(aggregateRootType);
        return tryGetAggregateAsync(aggregateRepository, aggregateRootType, aggregateRootId, 0);
    }

    private <T extends IAggregateRoot> CompletableFuture<T> tryGetAggregateAsync(IAggregateRepositoryProxy aggregateRepository, Class<?> aggregateRootType, String aggregateRootId, int retryTimes) {
        CompletableFuture<T> taskSource = new CompletableFuture<>();
        if (aggregateRepository == null) {
            taskSource.complete(null);
            return taskSource;
        }
        IOHelper.tryAsyncActionRecursively("TryGetAggregateAsync",
                () -> aggregateRepository.getAsync(aggregateRootId),
                result -> {
                    taskSource.complete((T) result);
                },
                () -> String.format("aggregateRepository.getAsync has unknown exception, aggregateRepository: %s, aggregateRootTypeName: %s, aggregateRootId: %s", aggregateRepository.getClass().getName(), aggregateRootType.getName(), aggregateRootId),
                null,
                retryTimes,
                true);
        return taskSource;
    }
}
