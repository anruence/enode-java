package com.qianzhui.enode.kafka.domainevent;

import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.eventing.DomainEventStreamMessage;
import com.qianzhui.enode.infrastructure.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

public class DomainEventPublisher implements IMessagePublisher<DomainEventStreamMessage> {

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage message) {
        return null;
    }
}
