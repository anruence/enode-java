package org.enodeframework.commanding.impl

import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.ICommandExecuteContext
import org.enodeframework.common.exception.AggregateRootAlreadyExistException
import org.enodeframework.common.io.Task
import org.enodeframework.common.utilities.Ensure
import org.enodeframework.domain.IAggregateRoot
import org.enodeframework.domain.IAggregateStorage
import org.enodeframework.domain.IRepository
import org.enodeframework.messaging.IApplicationMessage
import org.enodeframework.queue.IMessageContext
import org.enodeframework.queue.ISendReplyService
import org.enodeframework.queue.QueueMessage
import org.enodeframework.queue.command.CommandMessage
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * @author anruence@gmail.com
 */
class CommandExecuteContext(
        repository: IRepository,
        aggregateRootStorage: IAggregateStorage,
        queueMessage: QueueMessage,
        messageContext: IMessageContext,
        commandMessage: CommandMessage,
        sendReplyService: ISendReplyService
) : ICommandExecuteContext {
    private val trackingAggregateRootDict: ConcurrentMap<String, IAggregateRoot>
    private val repository: IRepository
    private val aggregateRootStorage: IAggregateStorage
    private val sendReplyService: ISendReplyService
    private val queueMessage: QueueMessage
    private val messageContext: IMessageContext
    private val commandMessage: CommandMessage
    override var result: String = ""
    override var applicationMessage: IApplicationMessage? = null
    override fun onCommandExecutedAsync(commandResult: CommandResult): CompletableFuture<Boolean> {
        messageContext.onMessageHandled(queueMessage)
        return if (Objects.isNull(commandMessage.replyAddress)) {
            Task.completedTask
        } else sendReplyService.sendCommandReply(commandResult, commandMessage.replyAddress)
    }

    override fun add(aggregateRoot: IAggregateRoot) {
        Ensure.notNull(aggregateRoot, "aggregateRoot")
        if (trackingAggregateRootDict.containsKey(aggregateRoot.uniqueId)) {
            throw AggregateRootAlreadyExistException(aggregateRoot.uniqueId, aggregateRoot.javaClass)
        }
        trackingAggregateRootDict[aggregateRoot.uniqueId] = aggregateRoot
    }

    /**
     * Add a new aggregate into the current command context synchronously, and then return a completed task object.
     */
    override fun addAsync(aggregateRoot: IAggregateRoot): CompletableFuture<Boolean> {
        add(aggregateRoot)
        return Task.completedTask
    }

    /**
     * Get an aggregate from the current command context.
     */
    override fun <T : IAggregateRoot> getAsync(id: Any, firstFromCache: Boolean, aggregateRootType: Class<T>): CompletableFuture<T> {
        Ensure.notNull(id, "id")
        val aggregateRootId = id.toString()
        val iAggregateRoot = trackingAggregateRootDict[aggregateRootId] as T?
        var future = CompletableFuture<T>()
        if (iAggregateRoot != null) {
            future.complete(iAggregateRoot)
            return future
        }
        future = if (firstFromCache) {
            repository.getAsync(aggregateRootType, id)
        } else {
            aggregateRootStorage.getAsync(aggregateRootType, aggregateRootId)
        }
        return future.thenApply { aggregateRoot: T? ->
            if (aggregateRoot != null) {
                trackingAggregateRootDict[aggregateRoot.uniqueId] = aggregateRoot
                repository.refreshAggregate(aggregateRoot)
            }
            aggregateRoot
        }
    }

    override fun <T : IAggregateRoot> getAsync(id: Any, clazz: Class<T>): CompletableFuture<T> {
        return getAsync(id, true, clazz)
    }

    override fun <T : IAggregateRoot> get(id: Any, firstFromCache: Boolean, clazz: Class<T>): T {
        return getAsync(id, firstFromCache, clazz).join()
    }

    override fun <T : IAggregateRoot> get(id: Any, clazz: Class<T>): T {
        return getAsync(id, clazz).join()
    }

    override val trackedAggregateRoots: List<IAggregateRoot>
        get() = ArrayList(trackingAggregateRootDict.values)

    override fun clear() {
        trackingAggregateRootDict.clear()
        result = ""
    }

    init {
        trackingAggregateRootDict = ConcurrentHashMap()
        this.repository = repository
        this.aggregateRootStorage = aggregateRootStorage
        this.sendReplyService = sendReplyService
        this.queueMessage = queueMessage
        this.commandMessage = commandMessage
        this.messageContext = messageContext
    }
}