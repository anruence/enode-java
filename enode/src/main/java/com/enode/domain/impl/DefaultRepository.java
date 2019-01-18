package com.enode.domain.impl;

import com.enode.common.utilities.CompletableFutureUtil;
import com.enode.domain.IAggregateRoot;
import com.enode.domain.IAggregateStorage;
import com.enode.domain.IMemoryCache;
import com.enode.domain.IRepository;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class DefaultRepository implements IRepository {
    private final IMemoryCache _memoryCache;
    private final IAggregateStorage _aggregateRootStorage;

    @Inject
    public DefaultRepository(IMemoryCache memoryCache, IAggregateStorage aggregateRootStorage) {
        _memoryCache = memoryCache;
        _aggregateRootStorage = aggregateRootStorage;
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, Object aggregateRootId) {
        if (aggregateRootType == null) {
            throw new NullPointerException("aggregateRootType");
        }
        if (aggregateRootId == null) {
            throw new NullPointerException("aggregateRootId");
        }
        CompletableFuture<T> future = _memoryCache.getAsync(aggregateRootId, aggregateRootType);
        return future.thenApply(aggregateRoot -> {
            if (aggregateRoot == null) {
                return CompletableFutureUtil.getValue(_aggregateRootStorage.getAsync(aggregateRootType, aggregateRootId.toString()));
            }
            return aggregateRoot;
        });
    }

    /**
     * Get an aggregate from memory cache, if not exist, get it from event store.
     *
     * @param aggregateRootId
     * @return
     */
    @Override
    public CompletableFuture<IAggregateRoot> getAsync(Object aggregateRootId) {
        return getAsync(IAggregateRoot.class, aggregateRootId);
    }
}
