package org.enodeframework.domain.impl;

import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IMemoryCache;
import org.enodeframework.domain.IRepository;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DefaultRepository implements IRepository {

    private final IMemoryCache memoryCache;

    public DefaultRepository(IMemoryCache memoryCache) {
        this.memoryCache = memoryCache;
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, Object aggregateRootId) {
        Ensure.notNull(aggregateRootType, "aggregateRootType");
        Ensure.notNull(aggregateRootId, "aggregateRootId");
        CompletableFuture<T> future = memoryCache.getAsync(aggregateRootId, aggregateRootType);
        return future.thenCompose(aggregateRoot -> {
            if (aggregateRoot == null) {
                return memoryCache.refreshAggregateFromEventStoreAsync(aggregateRootType, aggregateRootId.toString());
            }
            return CompletableFuture.completedFuture(aggregateRoot);
        });
    }

    /**
     * Get an aggregate from memory cache, if not exist, get it from event store.
     */
    @Override
    public CompletableFuture<IAggregateRoot> getAsync(Object aggregateRootId) {
        return getAsync(IAggregateRoot.class, aggregateRootId);
    }
}
