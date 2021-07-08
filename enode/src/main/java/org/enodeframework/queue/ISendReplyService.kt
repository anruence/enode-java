package org.enodeframework.queue

import org.enodeframework.commanding.CommandResult
import org.enodeframework.common.remoting.ReplySocketAddress
import org.enodeframework.queue.domainevent.DomainEventHandledMessage
import java.util.concurrent.CompletableFuture

interface ISendReplyService {
    /**
     * Send command handle result
     */
    fun sendCommandReply(commandResult: CommandResult, replyAddress: ReplySocketAddress): CompletableFuture<Boolean>

    /**
     * Send event handle result
     */
    fun sendEventReply(eventHandledMessage: DomainEventHandledMessage, replyAddress: ReplySocketAddress): CompletableFuture<Boolean>
}