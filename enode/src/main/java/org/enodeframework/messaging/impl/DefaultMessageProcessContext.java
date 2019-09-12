package org.enodeframework.messaging.impl;

import org.enodeframework.messaging.IEventProcessContext;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.QueueMessage;

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
    public void notifyEventProcessed() {
        messageContext.onMessageHandled(queueMessage);
    }
}
