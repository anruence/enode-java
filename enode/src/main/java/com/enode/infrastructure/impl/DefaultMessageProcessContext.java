package com.enode.infrastructure.impl;

import com.enode.infrastructure.IMessageProcessContext;
import com.enode.queue.CompletableConsumeConcurrentlyContext;

public class DefaultMessageProcessContext implements IMessageProcessContext {
    protected final String _queueMessage;
    protected final CompletableConsumeConcurrentlyContext _messageContext;

    public DefaultMessageProcessContext(String queueMessage, CompletableConsumeConcurrentlyContext messageContext) {
        _queueMessage = queueMessage;
        _messageContext = messageContext;
    }

    @Override
    public void notifyMessageProcessed() {
    }
}
