package com.enodeframework.domain.impl;

import com.enodeframework.common.io.Task;
import com.enodeframework.common.scheduling.IScheduleService;
import com.enodeframework.domain.AggregateCacheInfo;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.domain.IAggregateStorage;
import com.enodeframework.domain.IMemoryCache;
import com.enodeframework.infrastructure.ITypeNameProvider;
import com.enodeframework.common.exception.EnodeRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DefaultMemoryCache implements IMemoryCache {

    private static final Logger logger = LoggerFactory.getLogger(DefaultMemoryCache.class);
    private Object lockObj = new Object();
    private final ConcurrentMap<String, AggregateCacheInfo> aggregateRootInfoDict;
    private final int timeoutSeconds = 5000;
    private final int scanExpiredAggregateIntervalMilliseconds = 5000;
    private final String taskName;
    @Autowired
    private IAggregateStorage aggregateStorage;
    @Autowired
    private ITypeNameProvider typeNameProvider;
    @Autowired
    private IScheduleService scheduleService;

    public DefaultMemoryCache() {
        aggregateRootInfoDict = new ConcurrentHashMap<>();
        taskName = "CleanInactiveAggregates" + System.nanoTime() + new Random().nextInt(10000);
    }

    @Override
    public CompletableFuture<IAggregateRoot> getAsync(Object aggregateRootId, Class aggregateRootType) {
        if (aggregateRootId == null) {
            throw new NullPointerException("aggregateRootId");
        }
        if (aggregateRootType == null) {
            throw new NullPointerException("aggregateRootType");
        }
        AggregateCacheInfo aggregateRootInfo = aggregateRootInfoDict.get(aggregateRootId.toString());
        if (aggregateRootInfo == null) {
            return Task.completedFuture(null);
        }
        IAggregateRoot aggregateRoot = aggregateRootInfo.getAggregateRoot();
        if (aggregateRoot.getClass() != aggregateRootType) {
            throw new EnodeRuntimeException(String.format("Incorrect aggregate root type, aggregateRootId:%s, type:%s, expecting type:%s", aggregateRootId, aggregateRoot.getClass(), aggregateRootType));
        }
        if (aggregateRoot.getChanges().size() > 0) {
            CompletableFuture<IAggregateRoot> lastestAggregateRootFuture = aggregateStorage.getAsync(aggregateRootType, aggregateRootId.toString());
            return lastestAggregateRootFuture.thenApply(lastestAggregateRoot -> {
                if (lastestAggregateRoot != null) {
                    resetAggregateRootCache(lastestAggregateRoot);
                }
                return lastestAggregateRoot;
            });
        }
        return CompletableFuture.completedFuture(aggregateRoot);
    }

    /**
     * Get an aggregate from memory cache.
     *
     * @param aggregateRootId
     * @return
     */
    @Override
    public CompletableFuture<IAggregateRoot> getAsync(Object aggregateRootId) {
        return getAsync(aggregateRootId, IAggregateRoot.class);
    }

    @Override
    public CompletableFuture<Void> updateAggregateRootCache(IAggregateRoot aggregateRoot) {
        resetAggregateRootCache(aggregateRoot);
        return Task.completedTask;
    }

    @Override
    public CompletableFuture<IAggregateRoot> refreshAggregateFromEventStoreAsync(String aggregateRootTypeName, Object aggregateRootId) {
        if (aggregateRootTypeName == null) {
            throw new NullPointerException("aggregateRootTypeName");
        }
        Class aggregateRootType = typeNameProvider.getType(aggregateRootTypeName);
        if (aggregateRootType == null) {
            logger.error("Could not find aggregate root type by aggregate root type name [{}].", aggregateRootTypeName);
            return Task.completedFuture(null);
        }
        return refreshAggregateFromEventStoreAsync(aggregateRootType, aggregateRootId);
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> refreshAggregateFromEventStoreAsync(Class<T> aggregateRootType, Object aggregateRootId) {
        if (aggregateRootId == null) {
            throw new NullPointerException("aggregateRootId");
        }
        if (aggregateRootType == null) {
            throw new NullPointerException("aggregateRootType");
        }

        return aggregateStorage.getAsync(aggregateRootType, aggregateRootId.toString()).thenApply(aggregateRoot -> {
            if (aggregateRoot != null) {
                resetAggregateRootCache(aggregateRoot);
            }
            return aggregateRoot;
        }).exceptionally(ex -> {
            logger.error("Refresh aggregate from event store has unknown exception, aggregateRootTypeName:{}, aggregateRootId:{}", typeNameProvider.getTypeName(aggregateRootType), aggregateRootId, ex);
            return null;
        });
    }

    @Override
    public void start() {
        scheduleService.startTask(taskName, this::cleanInactiveAggregateRoot, scanExpiredAggregateIntervalMilliseconds, scanExpiredAggregateIntervalMilliseconds);
    }

    @Override
    public void stop() {
        scheduleService.stopTask(taskName);
    }

    private void resetAggregateRootCache(IAggregateRoot aggregateRoot) {
        synchronized (lockObj) {
            if (aggregateRoot == null) {
                throw new NullPointerException("aggregateRoot");
            }
            AggregateCacheInfo cacheInfo = aggregateRootInfoDict.computeIfAbsent(aggregateRoot.uniqueId(), x -> {
                if (logger.isDebugEnabled()) {

                    logger.debug("Aggregate root in-memory cache init, aggregateRootType: {}, aggregateRootId: {}, aggregateRootVersion: {}", aggregateRoot.getClass().getName(), aggregateRoot.uniqueId(), aggregateRoot.getVersion());
                }

                return new AggregateCacheInfo(aggregateRoot);
            });
            int aggregateRootOldVersion = cacheInfo.getAggregateRoot().getVersion();

            cacheInfo.setAggregateRoot(aggregateRoot);
            cacheInfo.setLastUpdateTimeMillis(System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("Aggregate root in-memory cache reset, aggregateRootType: {}, aggregateRootId: {}, aggregateRootNewVersion: {}, aggregateRootOldVersion: {}", aggregateRoot.getClass().getName(), aggregateRoot.uniqueId(), aggregateRoot.getVersion(), aggregateRootOldVersion);
            }
        }
    }

    private void cleanInactiveAggregateRoot() {
        List<Map.Entry<String, AggregateCacheInfo>> inactiveList = aggregateRootInfoDict.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired(timeoutSeconds))
                .collect(Collectors.toList());

        inactiveList.forEach(entry -> {
            if (aggregateRootInfoDict.remove(entry.getKey()) != null) {
                logger.info("Removed inactive aggregate root, id: {}", entry.getKey());
            }
        });
    }
}
