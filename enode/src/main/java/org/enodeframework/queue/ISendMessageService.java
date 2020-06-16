package org.enodeframework.queue;

import org.enodeframework.queue.QueueMessage;

import java.util.concurrent.CompletableFuture;

public interface ISendMessageService {
    CompletableFuture<Void> sendMessageAsync(QueueMessage queueMessage);
}
