package com.enode.domain.impl;

import com.enode.domain.IAggregateRoot;
import com.enode.domain.IAggregateStorage;
import com.enode.domain.IMemoryCache;
import com.enode.domain.IRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class DefaultRepository implements IRepository {

    @Autowired
    private IMemoryCache _memoryCache;

    @Autowired
    private IAggregateStorage _aggregateRootStorage;

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, Object aggregateRootId) {
        if (aggregateRootType == null) {
            throw new NullPointerException("aggregateRootType");
        }
        if (aggregateRootId == null) {
            throw new NullPointerException("aggregateRootId");
        }
        CompletableFuture<T> future = _memoryCache.getAsync(aggregateRootId, aggregateRootType);
        return future.thenCompose(aggregateRoot -> {
            if (aggregateRoot == null) {
                return _aggregateRootStorage.getAsync(aggregateRootType, aggregateRootId.toString());
            }
            return CompletableFuture.completedFuture(aggregateRoot);
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
