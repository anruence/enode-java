package org.enodeframework.domain.impl;

import org.enodeframework.common.exception.EnodeRuntimeException;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.scheduling.IScheduleService;
import org.enodeframework.domain.AggregateCacheInfo;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IAggregateStorage;
import org.enodeframework.domain.IMemoryCache;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final ConcurrentMap<String, AggregateCacheInfo> aggregateRootInfoDict;
    private final Object lockObj = new Object();
    private final String taskName;
    private final IAggregateStorage aggregateStorage;
    private final ITypeNameProvider typeNameProvider;
    private final IScheduleService scheduleService;
    private int timeoutSeconds = 5000;
    private int scanExpiredAggregateIntervalMilliseconds = 5000;

    public DefaultMemoryCache(IAggregateStorage aggregateStorage, IScheduleService scheduleService, ITypeNameProvider typeNameProvider) {
        this.aggregateStorage = aggregateStorage;
        this.typeNameProvider = typeNameProvider;
        this.scheduleService = scheduleService;
        aggregateRootInfoDict = new ConcurrentHashMap<>();
        taskName = "CleanInactiveAggregates_" + System.nanoTime() + new Random().nextInt(10000);
    }

    @Override
    public CompletableFuture<IAggregateRoot> getAsync(Object aggregateRootId, Class aggregateRootType) {
        if (aggregateRootId == null) {
            throw new IllegalArgumentException("aggregateRootId");
        }
        if (aggregateRootType == null) {
            throw new IllegalArgumentException("aggregateRootType");
        }
        AggregateCacheInfo aggregateRootInfo = aggregateRootInfoDict.get(aggregateRootId.toString());
        if (aggregateRootInfo == null) {
            return CompletableFuture.completedFuture(null);
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
        CompletableFuture<IAggregateRoot> future = new CompletableFuture<>();
        if (aggregateRootTypeName == null) {
            future.completeExceptionally(new IllegalArgumentException("aggregateRootTypeName"));
            return future;
        }
        try {
            Class aggregateRootType = typeNameProvider.getType(aggregateRootTypeName);
            if (aggregateRootType == null) {
                logger.error("Could not find aggregate root type by aggregate root type name [{}].", aggregateRootTypeName);
                future.complete(null);
                return future;
            }
            return refreshAggregateFromEventStoreAsync(aggregateRootType, aggregateRootId);
        } catch (Exception e) {
            future.completeExceptionally(e);
            return future;
        }
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> refreshAggregateFromEventStoreAsync(Class<T> aggregateRootType, Object aggregateRootId) {
        CompletableFuture<T> future = new CompletableFuture<>();
        if (aggregateRootId == null) {
            future.completeExceptionally(new IllegalArgumentException("aggregateRootId"));
            return future;
        }
        if (aggregateRootType == null) {
            future.completeExceptionally(new IllegalArgumentException("aggregateRootType"));
            return future;
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
                throw new IllegalArgumentException("aggregateRoot");
            }
            AggregateCacheInfo cacheInfo = aggregateRootInfoDict.computeIfAbsent(aggregateRoot.getUniqueId(), x -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Aggregate root in-memory cache init, aggregateRootType: {}, aggregateRootId: {}, aggregateRootVersion: {}", aggregateRoot.getClass().getName(), aggregateRoot.getUniqueId(), aggregateRoot.getVersion());
                }
                return new AggregateCacheInfo(aggregateRoot);
            });
            int aggregateRootOldVersion = cacheInfo.getAggregateRoot().getVersion();
            cacheInfo.setAggregateRoot(aggregateRoot);
            cacheInfo.setLastUpdateTimeMillis(System.currentTimeMillis());
            if (logger.isDebugEnabled()) {
                logger.debug("Aggregate root in-memory cache reset, aggregateRootType: {}, aggregateRootId: {}, aggregateRootNewVersion: {}, aggregateRootOldVersion: {}", aggregateRoot.getClass().getName(), aggregateRoot.getUniqueId(), aggregateRoot.getVersion(), aggregateRootOldVersion);
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

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getScanExpiredAggregateIntervalMilliseconds() {
        return scanExpiredAggregateIntervalMilliseconds;
    }

    public void setScanExpiredAggregateIntervalMilliseconds(int scanExpiredAggregateIntervalMilliseconds) {
        this.scanExpiredAggregateIntervalMilliseconds = scanExpiredAggregateIntervalMilliseconds;
    }
}
