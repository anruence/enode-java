package org.enodeframework.queue;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.queue.domainevent.DomainEventHandledMessage;

import java.util.concurrent.CompletableFuture;

public interface ISendReplyService {
    CompletableFuture<Void> sendCommandReply(CommandResult replyData, String replyAddress);

    CompletableFuture<Void> sendEventReply(DomainEventHandledMessage replyData, String replyAddress);
}
