package org.enodeframework.messaging.impl;

import org.enodeframework.common.io.Task;
import org.enodeframework.eventing.IEventProcessContext;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.QueueMessage;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DefaultMessageProcessContext implements IEventProcessContext {
    protected final QueueMessage queueMessage;
    protected final IMessageContext messageContext;

    public DefaultMessageProcessContext(QueueMessage queueMessage, IMessageContext messageContext) {
        this.queueMessage = queueMessage;
        this.messageContext = messageContext;
    }

    @Override
    public CompletableFuture<Boolean> notifyEventProcessed() {
        messageContext.onMessageHandled(queueMessage);
        return Task.completedTask;
    }
}
