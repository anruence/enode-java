package com.enode.infrastructure.impl;

import com.enode.infrastructure.IMessageProcessContext;
import com.enode.queue.IMessageContext;
import com.enode.queue.QueueMessage;

public class DefaultMessageProcessContext implements IMessageProcessContext {

    protected final QueueMessage _queueMessage;
    protected final IMessageContext _messageContext;

    public DefaultMessageProcessContext(QueueMessage queueMessage, IMessageContext messageContext) {
        _queueMessage = queueMessage;
        _messageContext = messageContext;
    }

    @Override
    public void notifyMessageProcessed() {
    }
}
