package org.enodeframework.eventing.impl

import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandStatus
import org.enodeframework.commanding.ProcessingCommand
import org.enodeframework.common.exception.MailBoxInvalidException
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.domain.IAggregateRoot
import org.enodeframework.domain.IMemoryCache
import org.enodeframework.eventing.*
import org.enodeframework.messaging.IMessagePublisher
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.stream.Collectors

/**
 * @author anruence@gmail.com
 */
class DefaultEventCommittingService(private val memoryCache: IMemoryCache, private val eventStore: IEventStore, private val serializeService: ISerializeService, private val domainEventPublisher: IMessagePublisher<DomainEventStreamMessage?>, private val eventMailBoxCount: Int, private val executor: Executor?) : IEventCommittingService {
    private val eventCommittingContextMailBoxList: MutableList<EventCommittingContextMailBox>

    constructor(memoryCache: IMemoryCache, eventStore: IEventStore, serializeService: ISerializeService, domainEventPublisher: IMessagePublisher<DomainEventStreamMessage?>, executor: Executor?) : this(memoryCache, eventStore, serializeService, domainEventPublisher, 4, executor) {}

    override fun commitDomainEventAsync(eventCommittingContext: EventCommittingContext) {
        val eventMailboxIndex = getEventMailBoxIndex(eventCommittingContext.eventStream.aggregateRootId)
        val eventMailbox = eventCommittingContextMailBoxList[eventMailboxIndex]
        eventMailbox.enqueueMessage(eventCommittingContext)
    }

    override fun publishDomainEventAsync(processingCommand: ProcessingCommand, eventStream: DomainEventStream) {
        if (eventStream.items == null || eventStream.items.size == 0) {
            eventStream.items = processingCommand.items
        }
        val eventStreamMessage = DomainEventStreamMessage(
                processingCommand.message.id,
                eventStream.aggregateRootId,
                eventStream.version,
                eventStream.aggregateRootTypeName,
                eventStream.events(),
                eventStream.items)
        publishDomainEventAsync(processingCommand, eventStreamMessage, 0)
    }

    private fun getEventMailBoxIndex(aggregateRootId: String): Int {
        var hash = 23
        for (c in aggregateRootId.toCharArray()) {
            hash = (hash shl 5) - hash + c.toInt()
        }
        if (hash < 0) {
            hash = Math.abs(hash)
        }
        return hash % eventMailBoxCount
    }

    private fun batchPersistEventAsync(committingContexts: List<EventCommittingContext>?, retryTimes: Int) {
        if (committingContexts == null || committingContexts.size == 0) {
            return
        }
        IOHelper.tryAsyncActionRecursively("BatchPersistEventAsync",
                { eventStore.batchAppendAsync(committingContexts.stream().map { obj: EventCommittingContext -> obj.eventStream }.collect(Collectors.toList())) },
                { result: EventAppendResult? ->
                    val eventMailBox = committingContexts.stream()
                            .findFirst()
                            .orElseThrow { MailBoxInvalidException("eventMailBox can not be null") }
                            .mailBox
                    if (result == null) {
                        logger.error("Batch persist events success, but the persist result is null, the current event committing mailbox should be pending, mailboxNumber: {}", eventMailBox.number)
                        return@tryAsyncActionRecursively
                    }
                    //针对持久化成功的聚合根，正常发布这些聚合根的事件到Q端
                    if (result.successAggregateRootIdList.size > 0) {
                        for (aggregateRootId in result.successAggregateRootIdList) {
                            val committingContextList = committingContexts.stream()
                                    .filter { x: EventCommittingContext -> x.eventStream.aggregateRootId == aggregateRootId }
                                    .collect(Collectors.toList())
                            if (committingContextList.size > 0) {
                                for (committingContext in committingContextList) {
                                    publishDomainEventAsync(committingContext.processingCommand, committingContext.eventStream)
                                }
                                if (logger.isDebugEnabled) {
                                    logger.debug("Batch persist events success, mailboxNumber: {}, aggregateRootId: {}",
                                            eventMailBox.number,
                                            aggregateRootId)
                                }
                            }
                        }
                    }
                    //针对持久化出现重复的命令ID，在命令MailBox中标记为已重复，在事件MailBox中清除对应聚合根产生的事件，且重新发布这些命令对应的领域事件到Q端
                    if (result.duplicateCommandAggregateRootIdList.size > 0) {
                        for ((key, value) in result.duplicateCommandAggregateRootIdList) {
                            val committingContextOptional = committingContexts.stream()
                                    .filter { x: EventCommittingContext -> key == x.eventStream.aggregateRootId }
                                    .findFirst()
                            if (committingContextOptional.isPresent) {
                                logger.warn("Batch persist events has duplicate commandIds, mailboxNumber: {}, aggregateRootId: {}, commandIds: {}",
                                        eventMailBox.number,
                                        key,
                                        java.lang.String.join(",", value))
                                val committingContext = committingContextOptional.get()
                                resetCommandMailBoxConsumingSequence(committingContext, committingContext.processingCommand.sequence, value)
                            }
                        }
                    }

                    //针对持久化出现版本冲突的聚合根，则自动处理每个聚合根的冲突
                    if (result.duplicateEventAggregateRootIdList.size > 0) {
                        for (aggregateRootId in result.duplicateEventAggregateRootIdList) {
                            val committingContextOptional = committingContexts.stream().filter { x: EventCommittingContext -> x.eventStream.aggregateRootId == aggregateRootId }.findFirst()
                            if (committingContextOptional.isPresent) {
                                logger.warn("Batch persist events, mailboxNumber: {}, duplicateEventAggregateRootCount: {}, detail: {}",
                                        eventMailBox.number,
                                        result.duplicateEventAggregateRootIdList.size,
                                        serializeService.serialize(result.duplicateEventAggregateRootIdList))
                                val eventCommittingContext = committingContextOptional.get()
                                if (eventCommittingContext.eventStream.version == 1) {
                                    handleFirstEventDuplicationAsync(eventCommittingContext, 0)
                                } else {
                                    resetCommandMailBoxConsumingSequence(eventCommittingContext, eventCommittingContext.processingCommand.sequence, null)
                                }
                            }
                        }
                    }
                    //最终，将当前的EventMailBox的本次处理标记为处理完成，然后继续可以处理下一批事件
                    eventMailBox.completeRun()
                },
                { String.format("[contextListCount:%d]", committingContexts.size) },
                null, retryTimes, true)
    }

    private fun resetCommandMailBoxConsumingSequence(context: EventCommittingContext, consumingSequence: Long, duplicateCommandIdList: List<String>?): CompletableFuture<Boolean> {
        val commandMailBox = context.processingCommand.mailBox
        val eventMailBox = context.mailBox
        val aggregateRootId = context.eventStream.aggregateRootId
        commandMailBox.pause()
        eventMailBox.removeAggregateAllEventCommittingContexts(aggregateRootId)
        return memoryCache.refreshAggregateFromEventStoreAsync(context.eventStream.aggregateRootTypeName, aggregateRootId).thenApply { x: IAggregateRoot? ->
            try {
                if (duplicateCommandIdList != null) {
                    for (commandId in duplicateCommandIdList) {
                        commandMailBox.addDuplicateCommandId(commandId)
                    }
                }
                commandMailBox.resetConsumingSequence(consumingSequence)
            } finally {
                commandMailBox.resume()
                commandMailBox.tryRun()
            }
            true
        }.exceptionally { ex: Throwable? ->
            logger.error("ResetCommandMailBoxConsumingSequence has unknown exception, aggregateRootId: {}", aggregateRootId, ex)
            null
        }
    }

    private fun tryToRepublishEventAsync(context: EventCommittingContext, retryTimes: Int) {
        val command = context.processingCommand.message
        IOHelper.tryAsyncActionRecursively("FindEventByCommandIdAsync",
                { eventStore.findAsync(context.eventStream.aggregateRootId, command.id) },
                { result: DomainEventStream? ->
                    if (result != null) {
                        //这里，我们需要再重新做一遍发布事件这个操作；
                        //之所以要这样做是因为虽然该command产生的事件已经持久化成功，但并不表示事件已经发布出去了；
                        //因为有可能事件持久化成功了，但那时正好机器断电了，则发布事件都没有做；
                        publishDomainEventAsync(context.processingCommand, result)
                    } else {
                        //到这里，说明当前command想添加到eventStore中时，提示command重复，但是尝试从eventStore中取出该command时却找不到该command。
                        //出现这种情况，我们就无法再做后续处理了，这种错误理论上不会出现，除非eventStore的Add接口和Get接口出现读写不一致的情况；
                        //框架会记录错误日志，让开发者排查具体是什么问题。
                        val errorMessage = String.format("Command should be exist in the event store, but we cannot find it from the event store, this should not be happen, and we cannot continue again. commandType:%s, commandId:%s, aggregateRootId:%s",
                                command.javaClass.name,
                                command.id,
                                context.eventStream.aggregateRootId)
                        logger.error(errorMessage)
                        val commandResult = CommandResult(CommandStatus.Failed, command.id, command.aggregateRootId, "Command should be exist in the event store, but we cannot find it from the event store.", String::class.java.name)
                        completeCommand(context.processingCommand, commandResult)
                    }
                },
                { String.format("[aggregateRootId:%s, commandId:%s]", command.aggregateRootId, command.id) },
                null,
                retryTimes, true)
    }

    private fun handleFirstEventDuplicationAsync(context: EventCommittingContext, retryTimes: Int): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        IOHelper.tryAsyncActionRecursively("FindFirstEventByVersion",
                { eventStore.findAsync(context.eventStream.aggregateRootId, 1) },
                { result: DomainEventStream? ->
                    if (result != null) {
                        //判断是否是同一个command，如果是，则再重新做一遍发布事件；
                        //之所以要这样做，是因为虽然该command产生的事件已经持久化成功，但并不表示事件也已经发布出去了；
                        //有可能事件持久化成功了，但那时正好机器断电了，则发布事件都没有做；
                        if (context.processingCommand.message.id == result.commandId) {
                            resetCommandMailBoxConsumingSequence(context, context.processingCommand.sequence + 1, null)
                                    .thenAccept {
                                        publishDomainEventAsync(context.processingCommand, result)
                                        future.complete(true)
                                    }
                        } else {
                            //如果不是同一个command，则认为是两个不同的command重复创建ID相同的聚合根，我们需要记录错误日志，然后通知当前command的处理完成；
                            val errorMessage = String.format("Duplicate aggregate creation. current commandId:%s, existing commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                                    context.processingCommand.message.id,
                                    result.commandId,
                                    result.aggregateRootId,
                                    result.aggregateRootTypeName)
                            logger.error(errorMessage)
                            resetCommandMailBoxConsumingSequence(context, context.processingCommand.sequence + 1, null)
                                    .thenAccept {
                                        val commandResult = CommandResult(CommandStatus.Failed, context.processingCommand.message.id, context.eventStream.aggregateRootId, "Duplicate aggregate creation.", String::class.java.name)
                                        completeCommand(context.processingCommand, commandResult)
                                                .thenAccept { future.complete(true) }
                                    }
                        }
                    } else {
                        val errorMessage = String.format("Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore. commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                                context.eventStream.commandId,
                                context.eventStream.aggregateRootId,
                                context.eventStream.aggregateRootTypeName)
                        logger.error(errorMessage)
                        resetCommandMailBoxConsumingSequence(context, context.processingCommand.sequence + 1, null).thenAccept {
                            val commandResult = CommandResult(CommandStatus.Failed, context.processingCommand.message.id, context.eventStream.aggregateRootId, "Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore.", String::class.java.name)
                            completeCommand(context.processingCommand, commandResult)
                                    .thenAccept { future.complete(true) }
                        }
                    }
                },
                { String.format("[eventStream:%s]", serializeService.serialize(context.eventStream)) },
                null, retryTimes, true)
        return future
    }

    private fun publishDomainEventAsync(processingCommand: ProcessingCommand, eventStream: DomainEventStreamMessage, retryTimes: Int) {
        IOHelper.tryAsyncActionRecursivelyWithoutResult("PublishDomainEventAsync",
                { domainEventPublisher.publishAsync(eventStream) },
                {
                    if (logger.isDebugEnabled) {
                        logger.debug("Publish domain events success, {}", serializeService.serialize(eventStream))
                    }
                    val commandHandleResult = processingCommand.commandExecuteContext.result
                    val commandResult = CommandResult(CommandStatus.Success, processingCommand.message.id, eventStream.getAggregateRootId(), commandHandleResult, String::class.java.name)
                    completeCommand(processingCommand, commandResult)
                },
                { String.format("[eventStream:%s]", serializeService.serialize(eventStream)) },
                null, retryTimes, true)
    }

    private fun completeCommand(processingCommand: ProcessingCommand, commandResult: CommandResult): CompletableFuture<Boolean> {
        return processingCommand.mailBox.completeMessage(processingCommand, commandResult)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultEventCommittingService::class.java)
    }

    init {
        eventCommittingContextMailBoxList = ArrayList()
        for (i in 0 until eventMailBoxCount) {
            val mailBox = EventCommittingContextMailBox(i, 1000, { x: List<EventCommittingContext>? -> batchPersistEventAsync(x, 0) }, executor)
            eventCommittingContextMailBoxList.add(mailBox)
        }
    }
}