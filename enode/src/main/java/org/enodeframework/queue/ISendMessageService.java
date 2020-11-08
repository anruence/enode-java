package org.enodeframework.queue;

import java.util.concurrent.CompletableFuture;

public interface ISendMessageService {
    CompletableFuture<Boolean> sendMessageAsync(QueueMessage queueMessage);
}
