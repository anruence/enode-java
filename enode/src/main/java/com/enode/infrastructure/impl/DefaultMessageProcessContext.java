package com.enode.infrastructure.impl;

import com.enode.infrastructure.IMessageProcessContext;
import com.enode.rocketmq.client.consumer.listener.CompletableConsumeConcurrentlyContext;

public class DefaultMessageProcessContext<T extends Object> implements IMessageProcessContext {
    protected final T _queueMessage;
    protected final CompletableConsumeConcurrentlyContext _messageContext;

    public DefaultMessageProcessContext(T queueMessage, CompletableConsumeConcurrentlyContext messageContext) {
        _queueMessage = queueMessage;
        _messageContext = messageContext;
    }

    @Override
    public void notifyMessageProcessed() {
    }
}
