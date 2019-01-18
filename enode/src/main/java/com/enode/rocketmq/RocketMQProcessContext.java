package com.enode.rocketmq;

import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.rocketmq.consumer.listener.CompletableConsumeConcurrentlyContext;
import com.enode.infrastructure.IMessageProcessContext;

public class RocketMQProcessContext implements IMessageProcessContext {
    protected final MessageExt _queueMessage;
    protected final CompletableConsumeConcurrentlyContext _messageContext;

    public RocketMQProcessContext(MessageExt queueMessage, CompletableConsumeConcurrentlyContext messageContext) {
        _queueMessage = queueMessage;
        _messageContext = messageContext;
    }

    @Override
    public void notifyMessageProcessed() {
        _messageContext.onMessageHandled();
    }
}
