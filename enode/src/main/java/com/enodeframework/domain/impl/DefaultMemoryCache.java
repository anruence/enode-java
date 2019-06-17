package com.enodeframework.domain.impl;

import com.enodeframework.common.io.Task;
import com.enodeframework.common.scheduling.IScheduleService;
import com.enodeframework.domain.AggregateCacheInfo;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.domain.IAggregateStorage;
import com.enodeframework.domain.IMemoryCache;
import com.enodeframework.infrastructure.ITypeNameProvider;
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
        AggregateCacheInfo aggregateRootInfo = aggregateRootInfoDict.get(aggregateRootId.toString());
        if (aggregateRootInfo == null) {
            return Task.CompletedTask;
        }
        IAggregateRoot aggregateRoot = aggregateRootInfo.getAggregateRoot();
        if (aggregateRoot.getClass() != aggregateRootType) {
            throw new RuntimeException(String.format("Incorrect aggregate root type, aggregateRootId:%s, type:%s, expecting type:%s", aggregateRootId, aggregateRoot.getClass(), aggregateRootType));
        }
        if (aggregateRoot.getChanges().size() > 0) {
            CompletableFuture<IAggregateRoot> lastestAggregateRootFuture = aggregateStorage.getAsync(aggregateRootType, aggregateRootId.toString());
            return lastestAggregateRootFuture.thenApply(lastestAggregateRoot -> {
                if (lastestAggregateRoot != null) {
                    setInternal(lastestAggregateRoot);
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
    public void set(IAggregateRoot aggregateRoot) {
        setInternal(aggregateRoot);
    }

    @Override
    public CompletableFuture<Void> refreshAggregateFromEventStoreAsync(String aggregateRootTypeName, String aggregateRootId) {
        try {
            Class aggregateRootType = typeNameProvider.getType(aggregateRootTypeName);
            if (aggregateRootType == null) {
                logger.error("Could not find aggregate root type by aggregate root type name [{}].", aggregateRootTypeName);
                return Task.CompletedTask;
            }
            CompletableFuture<IAggregateRoot> future = aggregateStorage.getAsync(aggregateRootType, aggregateRootId);
            return future.thenAccept(aggregateRoot -> {
                if (aggregateRoot != null) {
                    setInternal(aggregateRoot);
                }
            });
        } catch (Exception ex) {
            logger.error(String.format("Refresh aggregate from event store has unknown exception, aggregateRootTypeName:%s, aggregateRootId:%s", aggregateRootTypeName, aggregateRootId), ex);
        }
        return Task.CompletedTask;
    }

    @Override
    public void start() {
        scheduleService.startTask(taskName, this::cleanInactiveAggregateRoot, scanExpiredAggregateIntervalMilliseconds, scanExpiredAggregateIntervalMilliseconds);
    }

    @Override
    public void stop() {
        scheduleService.stopTask(taskName);
    }

    private void setInternal(IAggregateRoot aggregateRoot) {
        if (aggregateRoot == null) {
            throw new NullPointerException("aggregateRoot");
        }

        aggregateRootInfoDict.merge(aggregateRoot.uniqueId(), new AggregateCacheInfo(aggregateRoot), (oldValue, value) -> {
            oldValue.setAggregateRoot(aggregateRoot);
            oldValue.setLastUpdateTimeMillis(System.currentTimeMillis());

            if (logger.isDebugEnabled()) {
                logger.debug("In memory aggregate updated, type: {}, id: {}, version: {}", aggregateRoot.getClass().getName(), aggregateRoot.uniqueId(), aggregateRoot.version());
            }

            return oldValue;
        });
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
