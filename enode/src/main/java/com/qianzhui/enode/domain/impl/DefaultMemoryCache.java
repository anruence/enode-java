package com.qianzhui.enode.domain.impl;

import com.qianzhui.enode.ENode;
import com.qianzhui.enode.common.logging.ENodeLogger;
import com.qianzhui.enode.common.scheduling.IScheduleService;
import com.qianzhui.enode.domain.AggregateCacheInfo;
import com.qianzhui.enode.domain.IAggregateRoot;
import com.qianzhui.enode.domain.IAggregateStorage;
import com.qianzhui.enode.domain.IMemoryCache;
import com.qianzhui.enode.infrastructure.ITypeNameProvider;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class DefaultMemoryCache implements IMemoryCache {
    private static final Logger _logger = ENodeLogger.getLog();

    private final ConcurrentMap<String, AggregateCacheInfo> _aggregateRootInfoDict;
    private final IAggregateStorage _aggregateStorage;
    private final ITypeNameProvider _typeNameProvider;
    private final IScheduleService _scheduleService;
    private final int _timeoutSeconds;
    private final String _taskName;

    @Inject
    public DefaultMemoryCache(IScheduleService scheduleService, ITypeNameProvider typeNameProvider, IAggregateStorage aggregateStorage) {
        _scheduleService = scheduleService;
        _aggregateRootInfoDict = new ConcurrentHashMap<>();
        _typeNameProvider = typeNameProvider;
        _aggregateStorage = aggregateStorage;
        _timeoutSeconds = ENode.getInstance().getSetting().getAggregateRootMaxInactiveSeconds();
        _taskName = "CleanInactiveAggregates_" + System.nanoTime() + new Random().nextInt(10000);
    }

    @Override
    public CompletableFuture<IAggregateRoot> getAsync(Object aggregateRootId, Class aggregateRootType) {
        if (aggregateRootId == null) {
            throw new NullPointerException("aggregateRootId");
        }
        AggregateCacheInfo aggregateRootInfo = _aggregateRootInfoDict.get(aggregateRootId.toString());
        CompletableFuture<IAggregateRoot> promise = new CompletableFuture<>();
        if (aggregateRootInfo == null) {
            promise.complete(null);
            return promise;
        }
        IAggregateRoot aggregateRoot = aggregateRootInfo.getAggregateRoot();
        if (aggregateRoot.getClass() != aggregateRootType) {
            throw new RuntimeException(String.format("Incorrect aggregate root type, aggregateRootId:%s, type:%s, expecting type:%s", aggregateRootId, aggregateRoot.getClass(), aggregateRootType));
        }
        if (aggregateRoot.getChanges().size() > 0) {
            CompletableFuture<IAggregateRoot> future = _aggregateStorage.getAsync(aggregateRootType, aggregateRootId.toString());
            future.thenAccept(lastestAggregateRoot -> {
                if (lastestAggregateRoot != null) {
                    setInternal(lastestAggregateRoot);
                }
            });
            return future;
        }
        promise.complete(aggregateRoot);
        return promise;
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
    public CompletableFuture refreshAggregateFromEventStoreAsync(String aggregateRootTypeName, String aggregateRootId) {
        try {
            Class aggregateRootType = _typeNameProvider.getType(aggregateRootTypeName);
            if (aggregateRootType == null) {
                _logger.error("Could not find aggregate root type by aggregate root type name [{}].", aggregateRootTypeName);
                return CompletableFuture.completedFuture(null);
            }
            CompletableFuture<IAggregateRoot> future = _aggregateStorage.getAsync(aggregateRootType, aggregateRootId);
            future.thenAccept(aggregateRoot -> {
                if (aggregateRoot != null) {
                    setInternal(aggregateRoot);
                }
            });
            return future;
        } catch (Exception ex) {
            _logger.error(String.format("Refresh aggregate from event store has unknown exception, aggregateRootTypeName:%s, aggregateRootId:%s", aggregateRootTypeName, aggregateRootId), ex);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void start() {
        _scheduleService.startTask(_taskName, this::cleanInactiveAggregateRoot, ENode.getInstance().getSetting().getScanExpiredAggregateIntervalMilliseconds(), ENode.getInstance().getSetting().getScanExpiredAggregateIntervalMilliseconds());
    }

    @Override
    public void stop() {
        _scheduleService.stopTask(_taskName);
    }

    private void setInternal(IAggregateRoot aggregateRoot) {
        if (aggregateRoot == null) {
            throw new NullPointerException("aggregateRoot");
        }

        _aggregateRootInfoDict.merge(aggregateRoot.uniqueId(), new AggregateCacheInfo(aggregateRoot), (oldValue, value) -> {
            oldValue.setAggregateRoot(aggregateRoot);
            oldValue.setLastUpdateTimeMillis(System.currentTimeMillis());

            if (_logger.isDebugEnabled()) {
                _logger.debug("In memory aggregate updated, type: {}, id: {}, version: {}", aggregateRoot.getClass().getName(), aggregateRoot.uniqueId(), aggregateRoot.version());
            }

            return oldValue;
        });
    }

    private void cleanInactiveAggregateRoot() {
        List<Map.Entry<String, AggregateCacheInfo>> inactiveList = _aggregateRootInfoDict.entrySet().stream()
                .filter(entry -> entry.getValue().isExpired(_timeoutSeconds))
                .collect(Collectors.toList());

        inactiveList.forEach(entry -> {
            if (_aggregateRootInfoDict.remove(entry.getKey()) != null) {
                _logger.info("Removed inactive aggregate root, id: {}", entry.getKey());
            }
        });
    }
}
