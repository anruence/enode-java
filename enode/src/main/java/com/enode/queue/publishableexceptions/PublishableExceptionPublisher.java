package com.enode.queue.publishableexceptions;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.infrastructure.IPublishableException;
import com.enode.queue.IMQProducer;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class PublishableExceptionPublisher implements IMessagePublisher<IPublishableException> {
    private final IMQProducer _sendMessageService;

    @Inject
    public PublishableExceptionPublisher(IMQProducer sendQueueMessageService) {
        _sendMessageService = sendQueueMessageService;
    }

    public PublishableExceptionPublisher start() {
        return this;
    }

    public PublishableExceptionPublisher shutdown() {
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException exception) {
        return _sendMessageService.sendAsync(exception, exception.getRoutingKey() == null ? exception.id() : exception.getRoutingKey());
    }

}
