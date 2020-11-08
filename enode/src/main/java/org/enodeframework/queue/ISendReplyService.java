package org.enodeframework.queue;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.common.io.ReplySocketAddress;
import org.enodeframework.queue.domainevent.DomainEventHandledMessage;

import java.util.concurrent.CompletableFuture;

public interface ISendReplyService {
    CompletableFuture<Boolean> sendCommandReply(CommandResult replyData, ReplySocketAddress replyAddress);

    CompletableFuture<Boolean> sendEventReply(DomainEventHandledMessage replyData, ReplySocketAddress replyAddress);
}
