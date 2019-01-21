package com.enode.rocketmq.applicationmessage;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.rocketmq.IMQProducer;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class ApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {

    private final IMQProducer _sendMessageService;

    @Inject
    public ApplicationMessagePublisher(IMQProducer sendQueueMessageService) {
        _sendMessageService = sendQueueMessageService;
    }

    public ApplicationMessagePublisher start() {
        return this;
    }

    public ApplicationMessagePublisher shutdown() {
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        return _sendMessageService.sendAsync(message, message.getRoutingKey() == null ? message.id() : message.getRoutingKey());
    }
}
