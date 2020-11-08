package org.enodeframework.commanding.impl

import com.google.common.base.Strings
import org.enodeframework.commanding.*
import org.enodeframework.common.SysProperties
import org.enodeframework.common.io.IOHelper
import org.enodeframework.common.io.Task
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.domain.AggregateRootReferenceChangedException
import org.enodeframework.domain.IAggregateRoot
import org.enodeframework.domain.IDomainException
import org.enodeframework.domain.IMemoryCache
import org.enodeframework.eventing.*
import org.enodeframework.infrastructure.IObjectProxy
import org.enodeframework.infrastructure.ITypeNameProvider
import org.enodeframework.messaging.IApplicationMessage
import org.enodeframework.messaging.IMessagePublisher
import org.enodeframework.messaging.MessageHandlerData
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.function.Function
import java.util.stream.Collectors

/**
 * @author anruence@gmail.com
 */
class DefaultProcessingCommandHandler(private val eventStore: IEventStore, private val commandHandlerProvider: ICommandHandlerProvider, private val typeNameProvider: ITypeNameProvider, private val eventCommittingService: IEventCommittingService, private val memoryCache: IMemoryCache, private val applicationMessagePublisher: IMessagePublisher<IApplicationMessage>, private val exceptionPublisher: IMessagePublisher<IDomainException>, private val serializeService: ISerializeService) : IProcessingCommandHandler {
    override fun handleAsync(processingCommand: ProcessingCommand): CompletableFuture<Void> {
        val command = processingCommand.message
        if (Strings.isNullOrEmpty(command.aggregateRootId)) {
            val errorMessage = String.format("The aggregateRootId of command cannot be null or empty. commandType:%s, commandId:%s", command.javaClass.name, command.id)
            logger.error(errorMessage)
            return completeCommand(processingCommand, CommandStatus.Failed, String::class.java.name, errorMessage)
        }
        val findResult = getCommandHandler(processingCommand) { commandType: Class<*>? -> commandHandlerProvider.getHandlers(commandType) }
        when (findResult.findStatus) {
            HandlerFindStatus.Found -> {
                return handleCommandInternal(processingCommand, findResult.findHandler as ICommandHandlerProxy, 0)
            }
            HandlerFindStatus.TooManyHandlerData -> {
                logger.error("Found more than one command handler data, commandType:{}, commandId:{}", command.javaClass.name, command.id)
                return completeCommand(processingCommand, CommandStatus.Failed, String::class.java.name, "More than one command handler data found.")
            }
            HandlerFindStatus.TooManyHandler -> {
                logger.error("Found more than one command handler, commandType:{}, commandId:{}", command.javaClass.name, command.id)
                return completeCommand(processingCommand, CommandStatus.Failed, String::class.java.name, "More than one command handler found.")
            }
            HandlerFindStatus.NotFound -> {
                val errorMessage = String.format("No command handler found of command. commandType:%s, commandId:%s", command.javaClass.name, command.id)
                logger.error(errorMessage)
                return completeCommand(processingCommand, CommandStatus.Failed, String::class.java.name, errorMessage)
            }
            else -> return Task.completedTask
        }
    }

    private fun handleCommandInternal(processingCommand: ProcessingCommand, commandHandler: ICommandHandlerProxy, retryTimes: Int): CompletableFuture<Void> {
        val command = processingCommand.message
        val commandContext = processingCommand.commandExecuteContext
        val taskSource = CompletableFuture<Void>()
        commandContext.clear()
        if (processingCommand.isDuplicated) {
            return republishCommandEvents(processingCommand, 0)
        }
        IOHelper.tryAsyncActionRecursivelyWithoutResult("HandleCommandAsync",
                { commandHandler.handleAsync(commandContext, command) },
                {
                    if (logger.isDebugEnabled) {
                        logger.debug("Handle command success. handlerType:{}, commandType:{}, commandId:{}, aggregateRootId:{}",
                                commandHandler.innerObject.javaClass.name,
                                command.javaClass.name,
                                command.id,
                                command.aggregateRootId)
                    }
                    if (commandContext.applicationMessage != null) {
                        commitChangesAsync(processingCommand, true, commandContext.applicationMessage, null)
                                .thenAccept { taskSource.complete(null) }
                    } else {
                        try {
                            commitAggregateChanges(processingCommand).thenAccept { taskSource.complete(null) }.exceptionally { ex: Throwable ->
                                logger.error("Commit aggregate changes has unknown exception, this should not be happen, and we just complete the command, handlerType:{}, commandType:{}, commandId:{}, aggregateRootId:{}",
                                        commandHandler.innerObject.javaClass.name,
                                        command.javaClass.name,
                                        command.id,
                                        command.aggregateRootId, ex)
                                completeCommand(processingCommand, CommandStatus.Failed, ex.javaClass.name, "Unknown exception caught when committing changes of command.").thenAccept { taskSource.complete(null) }
                                null
                            }
                        } catch (aggregateRootReferenceChangedException: AggregateRootReferenceChangedException) {
                            logger.info("Aggregate root reference changed when processing command, try to re-handle the command. aggregateRootId: {}, aggregateRootType: {}, commandId: {}, commandType: {}, handlerType: {}",
                                    aggregateRootReferenceChangedException.aggregateRoot.uniqueId,
                                    aggregateRootReferenceChangedException.aggregateRoot.javaClass.name,
                                    command.id,
                                    command.javaClass.name,
                                    commandHandler.innerObject.javaClass.name
                            )
                            handleCommandInternal(processingCommand, commandHandler, 0).thenAccept { taskSource.complete(null) }
                        } catch (e: Exception) {
                            logger.error("Commit aggregate changes has unknown exception, this should not be happen, and we just complete the command, handlerType:{}, commandType:{}, commandId:{}, aggregateRootId:{}",
                                    commandHandler.innerObject.javaClass.name,
                                    command.javaClass.name,
                                    command.id,
                                    command.aggregateRootId, e)
                            completeCommand(processingCommand, CommandStatus.Failed, e.javaClass.name, "Unknown exception caught when committing changes of command.").thenAccept { taskSource.complete(null) }
                        }
                    }
                },
                { String.format("[command:[id:%s,type:%s],handlerType:%s,aggregateRootId:%s]", command.id, command.javaClass.name, commandHandler.innerObject.javaClass.name, command.aggregateRootId) },
                { ex: Throwable, errorMessage: String ->
                    handleExceptionAsync(processingCommand, commandHandler, ex, errorMessage, 0)
                            .thenAccept { taskSource.complete(null) }
                }, retryTimes, false)
        return taskSource
    }

    private fun commitAggregateChanges(processingCommand: ProcessingCommand): CompletableFuture<Void> {
        val command = processingCommand.message
        val context = processingCommand.commandExecuteContext
        val trackedAggregateRoots = context.trackedAggregateRoots
        var dirtyAggregateRootCount = 0
        var dirtyAggregateRoot: IAggregateRoot? = null
        var changedEvents: List<IDomainEvent<*>> = ArrayList()
        for (aggregateRoot in trackedAggregateRoots) {
            val events = aggregateRoot.changes
            if (events.size > 0) {
                dirtyAggregateRootCount++
                if (dirtyAggregateRootCount > 1) {
                    val errorMessage = String.format("Detected more than one aggregate created or modified by command. commandType:%s, commandId:%s",
                            command.javaClass.name,
                            command.id)
                    logger.error(errorMessage)
                    return completeCommand(processingCommand, CommandStatus.Failed, String::class.java.name, errorMessage)
                }
                dirtyAggregateRoot = aggregateRoot
                changedEvents = events
            }
        }
        //如果当前command没有对任何聚合根做修改，框架仍然需要尝试获取该command之前是否有产生事件，
        //如果有，则需要将事件再次发布到MQ；如果没有，则完成命令，返回command的结果为NothingChanged。
        //之所以要这样做是因为有可能当前command上次执行的结果可能是事件持久化完成，但是发布到MQ未完成，然后那时正好机器断电宕机了；
        //这种情况下，如果机器重启，当前command对应的聚合根从EventStore恢复的聚合根是被当前command处理过后的；
        //所以如果该command再次被处理，可能对应的聚合根就不会再产生事件了；
        //所以，我们要考虑到这种情况，尝试再次发布该命令产生的事件到MQ；
        //否则，如果我们直接将当前command设置为完成，即对MQ进行ack操作，那该command的事件就永远不会再发布到MQ了，这样就无法保证CQRS数据的最终一致性了。
        if (dirtyAggregateRootCount == 0 || changedEvents.isEmpty()) {
            return republishCommandEvents(processingCommand, 0)
        }
        dirtyAggregateRoot!!
        val eventStream = DomainEventStream(
                processingCommand.message.id,
                dirtyAggregateRoot.uniqueId,
                typeNameProvider.getTypeName(dirtyAggregateRoot.javaClass),
                Date(),
                changedEvents,
                command.items)
        //内存先接受聚合根的更新，需要检查聚合根引用是否已变化，如果已变化，会抛出异常
        return memoryCache.acceptAggregateRootChanges(dirtyAggregateRoot).thenAccept {
            val commandResult = processingCommand.commandExecuteContext.result
            processingCommand.items[SysProperties.ITEMS_COMMAND_RESULT_KEY] = commandResult
            //提交事件流进行后续的处理
            eventCommittingService.commitDomainEventAsync(EventCommittingContext(eventStream, processingCommand))
        }
    }

    private fun republishCommandEvents(processingCommand: ProcessingCommand, retryTimes: Int): CompletableFuture<Void> {
        val future = CompletableFuture<Void>()
        val command = processingCommand.message
        IOHelper.tryAsyncActionRecursively("ProcessIfNoEventsOfCommand",
                { eventStore.findAsync(command.aggregateRootId, command.id) },
                { result: DomainEventStream? ->
                    if (result != null) {
                        eventCommittingService.publishDomainEventAsync(processingCommand, result)
                        future.complete(null)
                    } else {
                        completeCommand(processingCommand, CommandStatus.NothingChanged, String::class.java.name, processingCommand.commandExecuteContext.result)
                                .thenAccept { future.complete(null) }
                    }
                },
                { String.format("[commandId:%s]", command.id) },
                null, retryTimes, true)
        return future
    }

    private fun handleExceptionAsync(processingCommand: ProcessingCommand, commandHandler: ICommandHandlerProxy, exception: Throwable, errorMessage: String, retryTimes: Int): CompletableFuture<Void?> {
        val command = processingCommand.message
        val future = CompletableFuture<Void?>()
        IOHelper.tryAsyncActionRecursively("FindEventByCommandIdAsync",
                { eventStore.findAsync(command.aggregateRootId, command.id) },
                { result: DomainEventStream? ->
                    if (result != null) {
                        //这里，我们需要再重新做一遍发布事件这个操作；
                        //之所以要这样做是因为虽然该command产生的事件已经持久化成功，但并不表示事件已经发布出去了；
                        //因为有可能事件持久化成功了，但那时正好机器断电了，则发布事件就没有做；
                        eventCommittingService.publishDomainEventAsync(processingCommand, result)
                        future.complete(null)
                    } else {
                        //到这里，说明当前command执行遇到异常，然后当前command之前也没执行过，是第一次被执行。
                        //那就判断当前异常是否是需要被发布出去的异常，如果是，则发布该异常给所有消费者；
                        //否则，就记录错误日志，然后认为该command处理失败即可；
                        val realException = getRealException(exception)
                        if (realException is IDomainException) {
                            publishExceptionAsync(processingCommand, realException as IDomainException, 0)
                                    .thenAccept { future.complete(null) }
                        } else {
                            completeCommand(processingCommand, CommandStatus.Failed, realException.javaClass.name, exception.message)
                                    .thenAccept { future.complete(null) }
                        }
                    }
                },
                { String.format("[command:[id:%s,type:%s],handlerType:%s,aggregateRootId:%s]", command.id, command.javaClass.name, commandHandler.innerObject.javaClass.name, command.aggregateRootId) },
                null, retryTimes, true
        )
        return future
    }

    private fun getRealException(exception: Throwable): Throwable {
        if (exception is CompletionException) {
            return Arrays.stream(exception.suppressed)
                    .filter { x: Throwable? -> x is IDomainException }
                    .findFirst()
                    .orElse(exception)
        }
        return exception
    }

    private fun publishExceptionAsync(processingCommand: ProcessingCommand, exception: IDomainException, retryTimes: Int): CompletableFuture<Void?> {
        exception.mergeItems(processingCommand.message.items)
        val future = CompletableFuture<Void?>()
        IOHelper.tryAsyncActionRecursivelyWithoutResult("PublishExceptionAsync",
                { exceptionPublisher.publishAsync(exception) },
                {
                    completeCommand(processingCommand, CommandStatus.Failed, exception.javaClass.name, (exception as Exception).message)
                            .thenAccept { future.complete(null) }
                },
                {
                    val serializableInfo: Map<String, Any> = HashMap()
                    exception.serializeTo(serializableInfo)
                    val exceptionInfo = serializableInfo.entries.stream().map { x: Map.Entry<String, Any> -> String.format("%s:%s", x.key, x.value) }.collect(Collectors.joining(","))
                    String.format("[commandId:%s, exceptionInfo:%s]", processingCommand.message.id, exceptionInfo)
                },
                null, retryTimes, true)
        return future
    }

    private fun commitChangesAsync(processingCommand: ProcessingCommand, success: Boolean, message: IApplicationMessage?, errorMessage: String?): CompletableFuture<Void> {
        if (success) {
            if (message != null) {
                message.mergeItems(processingCommand.message.items)
                return publishMessageAsync(processingCommand, message, 0)
            }
            return completeCommand(processingCommand, CommandStatus.Success, null, null)
        }
        return completeCommand(processingCommand, CommandStatus.Failed, String::class.java.name, errorMessage)
    }

    private fun publishMessageAsync(processingCommand: ProcessingCommand, message: IApplicationMessage, retryTimes: Int): CompletableFuture<Void> {
        val command = processingCommand.message
        val future = CompletableFuture<Void>()
        IOHelper.tryAsyncActionRecursivelyWithoutResult("PublishApplicationMessageAsync",
                { applicationMessagePublisher.publishAsync(message) },
                {
                    completeCommand(processingCommand, CommandStatus.Success, message.javaClass.name, serializeService.serialize(message))
                            .thenAccept { future.complete(null) }
                },
                { String.format("[application message:[id:%s,type:%s],command:[id:%s,type:%s]]", message.id, message.javaClass.name, command.id, command.javaClass.name) },
                null,
                retryTimes, true)
        return future
    }

    private fun <T : IObjectProxy?> getCommandHandler(processingCommand: ProcessingCommand, getHandlersFunc: Function<Class<*>, List<MessageHandlerData<T>?>?>): HandlerFindResult<*> {
        val command = processingCommand.message
        val handlerDataList = getHandlersFunc.apply(command.javaClass)
        if (handlerDataList == null || handlerDataList.isEmpty()) {
            return HandlerFindResult.NotFound
        } else if (handlerDataList.size > 1) {
            return HandlerFindResult.TooManyHandlerData
        }
        val handlerData = handlerDataList.stream().findFirst().orElse(MessageHandlerData())
        if (handlerData!!.listHandlers == null || handlerData.listHandlers.size == 0) {
            return HandlerFindResult.NotFound
        } else if (handlerData.listHandlers.size > 1) {
            return HandlerFindResult.TooManyHandler
        }
        return handlerData.listHandlers.stream().findFirst()
                .map { t: T -> HandlerFindResult(HandlerFindStatus.Found, t) }
                .orElseGet { HandlerFindResult.NotFound as HandlerFindResult<T>? }
    }

    private fun completeCommand(processingCommand: ProcessingCommand, commandStatus: CommandStatus, resultType: String?, result: String?): CompletableFuture<Void> {
        val commandResult = CommandResult(commandStatus, processingCommand.message.id, processingCommand.message.aggregateRootId, result, resultType)
        return processingCommand.mailBox.completeMessage(processingCommand, commandResult)
    }

    internal enum class HandlerFindStatus {
        NotFound, Found, TooManyHandlerData, TooManyHandler
    }

    internal class HandlerFindResult<T : IObjectProxy?>(var findStatus: HandlerFindStatus, var findHandler: T?) {

        constructor(findStatus: HandlerFindStatus) : this(findStatus, null)

        companion object {
            var NotFound: HandlerFindResult<*> = HandlerFindResult<IObjectProxy>(HandlerFindStatus.NotFound)
            var TooManyHandlerData: HandlerFindResult<*> = HandlerFindResult<IObjectProxy>(HandlerFindStatus.TooManyHandlerData)
            var TooManyHandler: HandlerFindResult<*> = HandlerFindResult<IObjectProxy>(HandlerFindStatus.TooManyHandler)
        }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultProcessingCommandHandler::class.java)
    }
}