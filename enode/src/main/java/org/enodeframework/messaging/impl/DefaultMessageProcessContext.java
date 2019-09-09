package org.enodeframework.messaging.impl;

import org.enodeframework.messaging.IMessageProcessContext;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.QueueMessage;

/**
 * @author anruence@gmail.com
 */
public class DefaultMessageProcessContext implements IMessageProcessContext {
    protected final QueueMessage queueMessage;
    protected final IMessageContext messageContext;

    public DefaultMessageProcessContext(QueueMessage queueMessage, IMessageContext messageContext) {
        this.queueMessage = queueMessage;
        this.messageContext = messageContext;
    }

    @Override
    public void notifyMessageProcessed() {
        messageContext.onMessageHandled(queueMessage);
    }
}
