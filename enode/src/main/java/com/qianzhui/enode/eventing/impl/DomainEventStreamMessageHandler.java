package com.qianzhui.enode.eventing.impl;

import com.qianzhui.enode.ENode;
import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.common.io.IOHelper;
import com.qianzhui.enode.eventing.DomainEventStreamMessage;
import com.qianzhui.enode.infrastructure.IMessageDispatcher;
import com.qianzhui.enode.infrastructure.IPublishedVersionStore;
import com.qianzhui.enode.infrastructure.ProcessingDomainEventStreamMessage;
import com.qianzhui.enode.infrastructure.impl.AbstractSequenceProcessingMessageHandler;

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
