package com.enode.queue.domainevent;

import com.enode.common.io.AsyncTaskResult;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.queue.IMQProducer;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class DomainEventPublisher implements IMessagePublisher<DomainEventStreamMessage> {

    private final IMQProducer _sendMessageService;

    @Inject
    public DomainEventPublisher(IMQProducer sendQueueMessageService) {
        _sendMessageService = sendQueueMessageService;
    }

    public DomainEventPublisher start() {
        return this;
    }

    public DomainEventPublisher shutdown() {
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage eventStream) {
        return _sendMessageService.sendAsync(eventStream, eventStream.getRoutingKey() == null ? eventStream.aggregateRootId() : eventStream.getRoutingKey());
    }

}
