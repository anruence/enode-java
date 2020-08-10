package org.enodeframework.queue;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.queue.domainevent.DomainEventHandledMessage;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public interface ISendReplyService {
    CompletableFuture<Void> sendCommandReply(CommandResult replyData, InetSocketAddress replyAddress);

    CompletableFuture<Void> sendEventReply(DomainEventHandledMessage replyData, InetSocketAddress replyAddress);
}
