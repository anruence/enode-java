package org.enodeframework.eventing.impl

import org.enodeframework.eventing.DomainEventStream
import org.enodeframework.eventing.EventAppendResult
import org.enodeframework.eventing.IEventStore
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Collectors

/**
 * @author anruence@gmail.com
 */
class InMemoryEventStore : IEventStore {
    private val lockObj = Any()
    private val aggregateInfoDict: ConcurrentMap<String, AggregateInfo>
    fun queryAggregateEvents(
        aggregateRootId: String,
        aggregateRootTypeName: String,
        minVersion: Int,
        maxVersion: Int
    ): List<DomainEventStream> {
        val eventStreams: List<DomainEventStream> = ArrayList()
        val aggregateInfo = aggregateInfoDict[aggregateRootId] ?: return eventStreams
        val min = Math.max(minVersion, 1)
        val max = Math.min(maxVersion, aggregateInfo.currentVersion)
        return aggregateInfo.eventDict.filter { x -> x.key in min..max }.map { x -> x.value }
    }

    override fun batchAppendAsync(eventStreams: List<DomainEventStream>): CompletableFuture<EventAppendResult> {
        val eventStreamDict: Map<String, List<DomainEventStream>> = eventStreams.stream().distinct()
            .collect(Collectors.groupingBy { obj: DomainEventStream -> obj.aggregateRootId })
        val eventAppendResult = EventAppendResult()
        val future = CompletableFuture<EventAppendResult>()
        for ((key, value) in eventStreamDict) {
            batchAppend(key, value, eventAppendResult)
        }
        future.complete(eventAppendResult)
        return future
    }

    override fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?> {
        return CompletableFuture.completedFuture(find(aggregateRootId, version))
    }

    override fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?> {
        return CompletableFuture.completedFuture(find(aggregateRootId, commandId))
    }

    override fun queryAggregateEventsAsync(
        aggregateRootId: String,
        aggregateRootTypeName: String,
        minVersion: Int,
        maxVersion: Int
    ): CompletableFuture<List<DomainEventStream>> {
        return CompletableFuture.completedFuture(
            queryAggregateEvents(
                aggregateRootId,
                aggregateRootTypeName,
                minVersion,
                maxVersion
            )
        )
    }

    private fun find(aggregateRootId: String, version: Int): DomainEventStream? {
        return aggregateInfoDict.get(aggregateRootId)?.eventDict?.get(version)
    }

    private fun find(aggregateRootId: String, commandId: String): DomainEventStream? {
        return aggregateInfoDict.get(aggregateRootId)?.commandDict?.get(commandId)
    }

    private fun batchAppend(
        aggregateRootId: String,
        eventStreamList: List<DomainEventStream>,
        eventAppendResult: EventAppendResult
    ) {
        synchronized(lockObj) {
            val aggregateInfo = aggregateInfoDict.computeIfAbsent(aggregateRootId) { AggregateInfo() }
            val optionalDomainEventStream = eventStreamList.stream().findFirst()
            if (optionalDomainEventStream.isPresent) {
                val firstEventStream = optionalDomainEventStream.get()
                //检查提交过来的第一个事件的版本号是否是当前聚合根的当前版本号的下一个版本号
                if (firstEventStream.version != aggregateInfo!!.currentVersion + 1) {
                    eventAppendResult.addDuplicateEventAggregateRootId(aggregateRootId)
                    return
                }
            }
            //检查提交过来的事件本身是否满足版本号的递增关系
            for (i in 0 until eventStreamList.size - 1) {
                if (eventStreamList[i + 1].version != eventStreamList[i].version + 1) {
                    eventAppendResult.addDuplicateEventAggregateRootId(aggregateRootId)
                    return
                }
            }

            //检查重复处理的命令ID
            val duplicateCommandIds: MutableList<String> = ArrayList()
            for (eventStream in eventStreamList) {
                if (aggregateInfo!!.commandDict.containsKey(eventStream.commandId)) {
                    duplicateCommandIds.add(eventStream.commandId)
                }
            }
            if (duplicateCommandIds.size > 0) {
                eventAppendResult.addDuplicateCommandIds(aggregateRootId, duplicateCommandIds)
                return
            }
            for (eventStream in eventStreamList) {
                aggregateInfo!!.eventDict[eventStream.version] = eventStream
                aggregateInfo.commandDict[eventStream.commandId] = eventStream
                aggregateInfo.currentVersion = eventStream.version
            }
            if (!eventAppendResult.successAggregateRootIdList.contains(aggregateRootId)) {
                eventAppendResult.addSuccessAggregateRootId(aggregateRootId)
            }
        }
    }

    internal inner class AggregateInfo {
        var status = AtomicBoolean(false)
        var currentVersion = 0
        var eventDict: ConcurrentMap<Int, DomainEventStream>
        var commandDict: ConcurrentMap<String, DomainEventStream>

        init {
            eventDict = ConcurrentHashMap()
            commandDict = ConcurrentHashMap()
        }
    }

    init {
        aggregateInfoDict = ConcurrentHashMap()
    }
}