package com.enode.eventing.impl;

import com.enode.ENode;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.IOHelper;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.infrastructure.IMessageDispatcher;
import com.enode.infrastructure.IPublishedVersionStore;
import com.enode.infrastructure.ProcessingDomainEventStreamMessage;
import com.enode.infrastructure.impl.AbstractSequenceProcessingMessageHandler;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class DomainEventStreamMessageHandler extends AbstractSequenceProcessingMessageHandler<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> {
    private final IMessageDispatcher _dispatcher;

    @Inject
    public DomainEventStreamMessageHandler(IPublishedVersionStore publishedVersionStore, IMessageDispatcher dispatcher, IOHelper ioHelper) {
        super(publishedVersionStore, ioHelper);
        _dispatcher = dispatcher;
    }

    @Override
    public String getName() {
        return ENode.getInstance().getSetting().getDomainEventStreamMessageHandlerName();
    }

    @Override
    protected CompletableFuture<AsyncTaskResult> dispatchProcessingMessageAsync(ProcessingDomainEventStreamMessage processingMessage) {
        return _dispatcher.dispatchMessagesAsync(processingMessage.getMessage().getEvents());
    }
}
