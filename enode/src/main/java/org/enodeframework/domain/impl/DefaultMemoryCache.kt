package org.enodeframework.domain.impl

import org.enodeframework.common.exception.AggregateRootTypeNotMatchException
import org.enodeframework.common.io.Task
import org.enodeframework.common.scheduling.IScheduleService
import org.enodeframework.common.utilities.Ensure
import org.enodeframework.domain.*
import org.enodeframework.infrastructure.ITypeNameProvider
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.function.Consumer
import java.util.stream.Collectors

/**
 * @author anruence@gmail.com
 */
class DefaultMemoryCache(private val aggregateStorage: IAggregateStorage, private val scheduleService: IScheduleService, private val typeNameProvider: ITypeNameProvider) : IMemoryCache {
    private val aggregateRootInfoDict: ConcurrentMap<String, AggregateCacheInfo?>
    private val lockObj = Any()
    private val taskName: String
    var timeoutSeconds = 5000
    var scanExpiredAggregateIntervalMilliseconds = 5000
    override fun <T : IAggregateRoot?> getAsync(aggregateRootId: Any, aggregateRootType: Class<T>): CompletableFuture<T> {
        Ensure.notNull(aggregateRootId, "aggregateRootId")
        Ensure.notNull(aggregateRootType, "aggregateRootType")
        val aggregateRootInfo = aggregateRootInfoDict[aggregateRootId.toString()]
                ?: return CompletableFuture.completedFuture(null)
        val aggregateRoot = aggregateRootInfo.aggregateRoot as T
        if (aggregateRootInfo.aggregateRoot.javaClass != aggregateRootType) {
            throw AggregateRootTypeNotMatchException(String.format("Incorrect aggregate root type, aggregateRootId:%s, type:%s, expecting type:%s", aggregateRootId, aggregateRootInfo.aggregateRoot.javaClass, aggregateRootType))
        }
        if (aggregateRoot!!.changes.size > 0) {
            val lastestAggregateRootFuture = aggregateStorage.getAsync(aggregateRootType, aggregateRootId.toString())
            return lastestAggregateRootFuture.thenApply { lastestAggregateRoot: IAggregateRoot? ->
                resetAggregateRootCache(aggregateRootType, aggregateRootId.toString(), lastestAggregateRoot)
                lastestAggregateRoot as T
            }
        }
        return CompletableFuture.completedFuture(aggregateRoot)
    }

    /**
     * Get an aggregate from memory cache.
     */
    override fun getAsync(aggregateRootId: Any): CompletableFuture<IAggregateRoot> {
        return getAsync(aggregateRootId, IAggregateRoot::class.java)
    }

    override fun acceptAggregateRootChanges(aggregateRoot: IAggregateRoot): CompletableFuture<Void> {
        synchronized(lockObj) {
            Ensure.notNull(aggregateRoot, "aggregateRoot")
            val cacheInfo = aggregateRootInfoDict.computeIfAbsent(aggregateRoot.uniqueId) { x: String? ->
                logger.info("Aggregate root in-memory cache initialized, aggregateRootType: {}, aggregateRootId: {}, aggregateRootVersion: {}", aggregateRoot.javaClass.name, aggregateRoot.uniqueId, aggregateRoot.version)
                AggregateCacheInfo(aggregateRoot)
            }
            //更新到内存缓存前需要先检查聚合根引用是否有变化，有变化说明此聚合根已经被重置过状态了
            if (aggregateRoot.version > 1 && cacheInfo!!.aggregateRoot !== aggregateRoot) {
                throw AggregateRootReferenceChangedException(aggregateRoot)
            }
            //接受聚合根的最新事件修改，更新聚合根版本号
            val aggregateRootOldVersion = cacheInfo!!.aggregateRoot.version
            aggregateRoot.acceptChanges()
            cacheInfo.updateAggregateRoot(aggregateRoot)
            logger.info("Aggregate root in-memory cache changed, aggregateRootType: {}, aggregateRootId: {}, aggregateRootNewVersion: {}, aggregateRootOldVersion: {}", aggregateRoot.javaClass.name, aggregateRoot.uniqueId, aggregateRoot.version, aggregateRootOldVersion)
        }
        return Task.completedTask
    }

    override fun refreshAggregateFromEventStoreAsync(aggregateRootTypeName: String, aggregateRootId: String): CompletableFuture<IAggregateRoot> {
        Ensure.notNull(aggregateRootTypeName, "aggregateRootTypeName")
        val future = CompletableFuture<IAggregateRoot>()
        return try {
            val aggregateRootType = typeNameProvider.getType(aggregateRootTypeName) as Class<IAggregateRoot>
            if (aggregateRootType == null) {
                logger.error("Could not find aggregate root type by aggregate root type name [{}].", aggregateRootTypeName)
                future.complete(null)
                return future
            }
            refreshAggregateFromEventStoreAsync(aggregateRootType, aggregateRootId)
        } catch (e: Exception) {
            future.completeExceptionally(e)
            future
        }
    }

    override fun <T : IAggregateRoot?> refreshAggregateFromEventStoreAsync(aggregateRootType: Class<T>, aggregateRootId: String): CompletableFuture<T> {
        Ensure.notNull(aggregateRootId, "aggregateRootId")
        Ensure.notNull(aggregateRootType, "aggregateRootType")
        return aggregateStorage.getAsync(aggregateRootType, aggregateRootId).thenApply { aggregateRoot: T ->
            resetAggregateRootCache(aggregateRootType, aggregateRootId, aggregateRoot)
            aggregateRoot
        }.exceptionally { ex: Throwable? ->
            logger.error("Refresh aggregate from event store has unknown exception, aggregateRootTypeName:{}, aggregateRootId:{}", typeNameProvider.getTypeName(aggregateRootType), aggregateRootId, ex)
            null
        }
    }

    override fun start() {
        scheduleService.startTask(taskName, { cleanInactiveAggregateRoot() }, scanExpiredAggregateIntervalMilliseconds, scanExpiredAggregateIntervalMilliseconds)
    }

    override fun stop() {
        scheduleService.stopTask(taskName)
    }

    private fun resetAggregateRootCache(aggregateRootType: Class<*>, aggregateRootId: String, aggregateRoot: IAggregateRoot?) {
        val aggregateCacheInfo = aggregateRootInfoDict.remove(aggregateRootId)
        if (aggregateCacheInfo != null) {
            logger.info("Removed dirty in-memory aggregate, aggregateRootType: {}, aggregateRootId: {}, version: {}", aggregateRootType.name, aggregateRootId, aggregateCacheInfo.aggregateRoot.version)
        }
        if (aggregateRoot == null) {
            return
        }
        synchronized(lockObj) {
            val cacheInfo = aggregateRootInfoDict.computeIfAbsent(aggregateRoot.uniqueId) { x: String? ->
                if (logger.isDebugEnabled) {
                    logger.debug("Aggregate root in-memory cache reset, aggregateRootType: {}, aggregateRootId: {}, aggregateRootVersion: {}", aggregateRoot.javaClass.name, aggregateRoot.uniqueId, aggregateRoot.version)
                }
                AggregateCacheInfo(aggregateRoot)
            }
            val aggregateRootOldVersion = cacheInfo!!.aggregateRoot.version
            cacheInfo.updateAggregateRoot(aggregateRoot)
            if (logger.isDebugEnabled) {
                logger.debug("Aggregate root in-memory cache reset, aggregateRootType: {}, aggregateRootId: {}, aggregateRootNewVersion: {}, aggregateRootOldVersion: {}", aggregateRoot.javaClass.name, aggregateRoot.uniqueId, aggregateRoot.version, aggregateRootOldVersion)
            }
        }
    }

    private fun cleanInactiveAggregateRoot() {
        val inactiveList: List<Map.Entry<String, AggregateCacheInfo?>> = aggregateRootInfoDict.entries.stream()
                .filter { entry: Map.Entry<String, AggregateCacheInfo?> -> entry.value!!.isExpired(timeoutSeconds) }
                .collect(Collectors.toList())
        inactiveList.forEach(Consumer { entry: Map.Entry<String, AggregateCacheInfo?> ->
            if (aggregateRootInfoDict.remove(entry.key) != null) {
                logger.info("Removed inactive aggregate root, id: {}", entry.key)
            }
        })
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultMemoryCache::class.java)
    }

    init {
        aggregateRootInfoDict = ConcurrentHashMap()
        taskName = "CleanInactiveAggregates_" + System.nanoTime() + Random().nextInt(10000)
    }
}