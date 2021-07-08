package org.enodeframework.domain.impl;

import org.enodeframework.common.exception.AggregateRootInvalidException;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IAggregateSnapshotter;
import org.enodeframework.domain.IAggregateStorage;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class SnapshotOnlyAggregateStorage implements IAggregateStorage {

    private final IAggregateSnapshotter aggregateSnapshotter;

    public SnapshotOnlyAggregateStorage(IAggregateSnapshotter aggregateSnapshotter) {
        this.aggregateSnapshotter = aggregateSnapshotter;
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, String aggregateRootId) {
        Assert.nonNull(aggregateRootId, "aggregateRootId");
        Assert.nonNull(aggregateRootType, "aggregateRootType");
        return tryRestoreFromSnapshotAsync(aggregateRootType, aggregateRootId, 0, new CompletableFuture<>())
                .thenApply(aggregateRoot -> {
                    if (aggregateRoot != null && (aggregateRoot.getClass() != aggregateRootType || !aggregateRoot.getUniqueId().equals(aggregateRootId))) {
                        throw new AggregateRootInvalidException(String.format("AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType: %s, aggregateRootId: %s], expected: [aggregateRootType: %s, aggregateRootId: %s]",
                                aggregateRoot.getClass(),
                                aggregateRoot.getUniqueId(),
                                aggregateRootType,
                                aggregateRootId));
                    }
                    return aggregateRoot;
                });
    }

    private <T extends IAggregateRoot> CompletableFuture<T> tryRestoreFromSnapshotAsync(Class<T> aggregateRootType, String aggregateRootId, int retryTimes, CompletableFuture<T> taskSource) {
        IOHelper.tryAsyncActionRecursively("TryRestoreFromSnapshotAsync",
                () -> aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId),
                taskSource::complete,
                () -> String.format("aggregateSnapshotter.tryRestoreFromSnapshotAsync has unknown exception, aggregateRootType: %s, aggregateRootId: %s", aggregateRootType.getName(), aggregateRootId),
                null, retryTimes, true);
        return taskSource;
    }
}
