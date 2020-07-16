package org.enodeframework.queue;

import java.util.concurrent.CompletableFuture;

public interface ISendMessageService {
    CompletableFuture<Void> sendMessageAsync(QueueMessage queueMessage);
}
