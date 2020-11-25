package org.enodeframework.domain.impl;

import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.utilities.Ensure;
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
        if (aggregateRepository == null) {
            return CompletableFuture.completedFuture(null);
        }
        Ensure.notNull(aggregateRepository, "aggregateRepository");
        return tryGetAggregateAsync(aggregateRepository, aggregateRootType, aggregateRootId, 0);
    }

    private <T extends IAggregateRoot> CompletableFuture<T> tryGetAggregateAsync(IAggregateRepositoryProxy aggregateRepository, Class<?> aggregateRootType, String aggregateRootId, int retryTimes) {
        CompletableFuture<T> taskSource = new CompletableFuture<>();
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
