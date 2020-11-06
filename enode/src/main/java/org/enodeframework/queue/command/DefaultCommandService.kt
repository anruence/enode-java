package org.enodeframework.queue.command

import org.enodeframework.commanding.CommandResult
import org.enodeframework.commanding.CommandReturnType
import org.enodeframework.commanding.ICommand
import org.enodeframework.commanding.ICommandService
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.common.utilities.Ensure
import org.enodeframework.common.utilities.InetUtil
import org.enodeframework.queue.ISendMessageService
import org.enodeframework.queue.QueueMessage
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class DefaultCommandService(private val topic: String, private val tag: String, private val commandResultProcessor: ICommandResultProcessor, private val sendMessageService: ISendMessageService, private val serializeService: ISerializeService) : ICommandService {
    override fun sendAsync(command: ICommand): CompletableFuture<Void> {
        return sendMessageService.sendMessageAsync(buildCommandMessage(command, false))
    }

    override fun executeAsync(command: ICommand): CompletableFuture<CommandResult> {
        return executeAsync(command, CommandReturnType.CommandExecuted)
    }

    override fun executeAsync(command: ICommand, commandReturnType: CommandReturnType): CompletableFuture<CommandResult> {
        val taskCompletionSource = CompletableFuture<CommandResult>()
        try {
            Ensure.notNull(commandResultProcessor, "commandResultProcessor")
            commandResultProcessor.registerProcessingCommand(command, commandReturnType, taskCompletionSource)
            val sendMessageAsync = sendMessageService.sendMessageAsync(buildCommandMessage(command, true))
            sendMessageAsync.thenAccept { sendResult: Void? -> }.exceptionally { ex: Throwable? ->
                commandResultProcessor.processFailedSendingCommand(command)
                taskCompletionSource.completeExceptionally(ex)
                null
            }
        } catch (ex: Exception) {
            taskCompletionSource.completeExceptionally(ex)
        }
        return taskCompletionSource
    }

    protected fun buildCommandMessage(command: ICommand, needReply: Boolean): QueueMessage {
        Ensure.notNull(command.aggregateRootId, "aggregateRootId")
        Ensure.notNull(topic, "topic")
        val commandData = serializeService.serialize(command)
        val commandMessage = CommandMessage()
        if (needReply) {
            val replyAddress = InetUtil.toSocketAddress(commandResultProcessor.bindAddress)
            commandMessage.replyAddress = replyAddress
        }
        commandMessage.commandData = commandData
        commandMessage.commandType = command.javaClass.name
        val messageData = serializeService.serialize(commandMessage)
        val queueMessage = QueueMessage()
        queueMessage.topic = topic
        queueMessage.tag = tag
        queueMessage.body = messageData
        queueMessage.routeKey = command.aggregateRootId
        val key = String.format("%s%s", command.id, Optional.ofNullable(command.aggregateRootId)
                .map { x: String -> "_cmd_agg_$x" }.orElse(""))
        queueMessage.key = key
        return queueMessage
    }
}