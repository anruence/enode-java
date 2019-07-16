package org.enodeframework.domain.impl;

import org.enodeframework.common.exception.AggregateRootTypeNotMatchException;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.scheduling.IScheduleService;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.domain.AggregateCacheInfo;
import org.enodeframework.domain.AggregateRootReferenceChangedException;
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
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object aggregateRootId, Class<T> aggregateRootType) {
        Ensure.notNull(aggregateRootId, "aggregateRootId");
        Ensure.notNull(aggregateRootType, "aggregateRootType");
        AggregateCacheInfo aggregateRootInfo = aggregateRootInfoDict.get(aggregateRootId.toString());
        if (aggregateRootInfo == null) {
            return CompletableFuture.completedFuture(null);
        }
        T aggregateRoot = (T) aggregateRootInfo.getAggregateRoot();
        if (aggregateRoot.getClass() != aggregateRootType) {
            throw new AggregateRootTypeNotMatchException(String.format("Incorrect aggregate root type, aggregateRootId:%s, type:%s, expecting type:%s", aggregateRootId, aggregateRoot.getClass(), aggregateRootType));
        }
        if (aggregateRoot.getChanges().size() > 0) {
            CompletableFuture<T> lastestAggregateRootFuture = aggregateStorage.getAsync(aggregateRootType, aggregateRootId.toString());
            return lastestAggregateRootFuture.thenApply(lastestAggregateRoot -> {
                resetAggregateRootCache(aggregateRootType, aggregateRootId.toString(), lastestAggregateRoot);
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
    public CompletableFuture<Void> acceptAggregateRootChanges(IAggregateRoot aggregateRoot) {
        synchronized (lockObj) {
            Ensure.notNull(aggregateRoot, "aggregateRoot");
            AggregateCacheInfo cacheInfo = aggregateRootInfoDict.computeIfAbsent(aggregateRoot.getUniqueId(), x -> {
                logger.info("Aggregate root in-memory cache initialized, aggregateRootType: {}, aggregateRootId: {}, aggregateRootVersion: {}", aggregateRoot.getClass().getName(), aggregateRoot.getUniqueId(), aggregateRoot.getVersion());
                return new AggregateCacheInfo(aggregateRoot);
            });
            //更新到内存缓存前需要先检查聚合根引用是否有变化，有变化说明此聚合根已经被重置过状态了
            if (aggregateRoot.getVersion() > 1 && cacheInfo.getAggregateRoot() != aggregateRoot) {
                throw new AggregateRootReferenceChangedException(aggregateRoot);
            }
            //接受聚合根的最新事件修改，更新聚合根版本号
            int aggregateRootOldVersion = cacheInfo.getAggregateRoot().getVersion();
            aggregateRoot.acceptChanges();
            cacheInfo.updateAggregateRoot(aggregateRoot);
            logger.info("Aggregate root in-memory cache changed, aggregateRootType: {}, aggregateRootId: {}, aggregateRootNewVersion: {}, aggregateRootOldVersion: {}", aggregateRoot.getClass().getName(), aggregateRoot.getUniqueId(), aggregateRoot.getVersion(), aggregateRootOldVersion);
        }
        return Task.completedTask;
    }

    @Override
    public CompletableFuture<IAggregateRoot> refreshAggregateFromEventStoreAsync(String aggregateRootTypeName, String aggregateRootId) {
        Ensure.notNull(aggregateRootTypeName, "aggregateRootTypeName");
        CompletableFuture<IAggregateRoot> future = new CompletableFuture<>();
        try {
            Class<IAggregateRoot> aggregateRootType = (Class<IAggregateRoot>) typeNameProvider.getType(aggregateRootTypeName);
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
    public <T extends IAggregateRoot> CompletableFuture<T> refreshAggregateFromEventStoreAsync(Class<T> aggregateRootType, String aggregateRootId) {
        Ensure.notNull(aggregateRootId, "aggregateRootId");
        Ensure.notNull(aggregateRootType, "aggregateRootType");
        return aggregateStorage.getAsync(aggregateRootType, aggregateRootId).thenApply(aggregateRoot -> {
            resetAggregateRootCache(aggregateRootType, aggregateRootId, aggregateRoot);
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

    private void resetAggregateRootCache(Class<?> aggregateRootType, String aggregateRootId, IAggregateRoot aggregateRoot) {
        AggregateCacheInfo aggregateCacheInfo = aggregateRootInfoDict.remove(aggregateRootId);
        if (aggregateCacheInfo != null) {
            logger.info("Removed dirty in-memory aggregate, aggregateRootType: {}, aggregateRootId: {}, version: {}", aggregateRootType.getName(), aggregateRootId, aggregateCacheInfo.getAggregateRoot().getVersion());
        }
        if (aggregateRoot == null) {
            return;
        }
        synchronized (lockObj) {
            AggregateCacheInfo cacheInfo = aggregateRootInfoDict.computeIfAbsent(aggregateRoot.getUniqueId(), x -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("Aggregate root in-memory cache reset, aggregateRootType: {}, aggregateRootId: {}, aggregateRootVersion: {}", aggregateRoot.getClass().getName(), aggregateRoot.getUniqueId(), aggregateRoot.getVersion());
                }
                return new AggregateCacheInfo(aggregateRoot);
            });
            int aggregateRootOldVersion = cacheInfo.getAggregateRoot().getVersion();
            cacheInfo.updateAggregateRoot(aggregateRoot);
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
