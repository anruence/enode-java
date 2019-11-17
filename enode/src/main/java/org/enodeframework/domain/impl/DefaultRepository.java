package org.enodeframework.domain.impl;

import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IMemoryCache;
import org.enodeframework.domain.IRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DefaultRepository implements IRepository {
    @Autowired
    private IMemoryCache memoryCache;

    public DefaultRepository setMemoryCache(IMemoryCache memoryCache) {
        this.memoryCache = memoryCache;
        return this;
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, Object aggregateRootId) {
        if (aggregateRootType == null) {
            throw new IllegalArgumentException("aggregateRootType");
        }
        if (aggregateRootId == null) {
            throw new IllegalArgumentException("aggregateRootId");
        }
        CompletableFuture<T> future = memoryCache.getAsync(aggregateRootId, aggregateRootType);
        return future.thenCompose(aggregateRoot -> {
            if (aggregateRoot == null) {
                return memoryCache.refreshAggregateFromEventStoreAsync(aggregateRootType, aggregateRootId);
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
