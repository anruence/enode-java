package com.enode.infrastructure.impl;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.IOHelper;
import com.enode.common.logging.ENodeLogger;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IMessageDispatcher;
import com.enode.infrastructure.IProcessingMessage;
import com.enode.infrastructure.IProcessingMessageHandler;
import com.enode.infrastructure.IPublishedVersionStore;
import com.enode.infrastructure.ProcessingDomainEventStreamMessage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class DefaultProcessingMessageHandler<X extends IProcessingMessage<X, Y>, Y extends IMessage> implements IProcessingMessageHandler<X, Y> {

    private static final Logger _logger = ENodeLogger.getLog();
    private final String domainEventStreamMessageHandlerName = "DefaultEventProcessor";
    @Autowired
    private IMessageDispatcher _dispatcher;
    @Autowired
    private IPublishedVersionStore _publishedVersionStore;
    @Autowired
    private IOHelper _ioHelper;

    @Override
    public void handleAsync(X processingMessage) {
        if (processingMessage instanceof ProcessingDomainEventStreamMessage) {
            handleAsyncDomain((ProcessingDomainEventStreamMessage) processingMessage);
            return;
        }
        CompletableFuture<AsyncTaskResult> asyncTaskResultCompletableFuture = _dispatcher.dispatchMessageAsync(processingMessage.getMessage());
        asyncTaskResultCompletableFuture.thenRun(() ->
                processingMessage.complete()
        );
    }

    private void handleAsyncDomain(ProcessingDomainEventStreamMessage processingMessage) {
        handleMessageAsync(processingMessage, 0);
    }

    public String getName() {
        return domainEventStreamMessageHandlerName;
    }

    private CompletableFuture<AsyncTaskResult> dispatchProcessingMessageAsync(ProcessingDomainEventStreamMessage processingMessage) {
        return _dispatcher.dispatchMessagesAsync(processingMessage.getMessage().getEvents());
    }

    private void handleMessageAsync(ProcessingDomainEventStreamMessage processingMessage, int retryTimes) {
        DomainEventStreamMessage message = processingMessage.getMessage();

        _ioHelper.tryAsyncActionRecursively("GetPublishedVersionAsync",
                () -> _publishedVersionStore.getPublishedVersionAsync(getName(), message.aggregateRootTypeName(), message.aggregateRootStringId()),
                currentRetryTimes -> handleMessageAsync(processingMessage, currentRetryTimes),
                result ->
                {
                    Integer publishedVersion = result.getData();
                    if (publishedVersion + 1 == message.version()) {
                        doDispatchProcessingMessageAsync(processingMessage, 0);
                    } else if (publishedVersion + 1 < message.version()) {
                        _logger.info("The sequence message cannot be process now as the version is not the next version, it will be handle later. contextInfo [aggregateRootId={},lastPublishedVersion={},messageVersion={}]", message.aggregateRootStringId(), publishedVersion, message.version());
                        processingMessage.addToWaitingList();
                    } else {
                        processingMessage.complete();
                    }
                },
                () -> String.format("sequence message [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%s]", message.id(), message.getClass().getName(), message.aggregateRootStringId(), message.version()),
                errorMessage ->

                        _logger.error(String.format("Get published version has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
    }

    private void doDispatchProcessingMessageAsync(ProcessingDomainEventStreamMessage processingMessage, int retryTimes) {
        _ioHelper.tryAsyncActionRecursively("DispatchProcessingMessageAsync",
                () -> dispatchProcessingMessageAsync(processingMessage),
                currentRetryTimes -> doDispatchProcessingMessageAsync(processingMessage, currentRetryTimes),
                result -> updatePublishedVersionAsync(processingMessage, 0),
                () -> String.format("sequence message [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%d]", processingMessage.getMessage().id(), processingMessage.getMessage().getClass().getName(), processingMessage.getMessage().aggregateRootStringId(), processingMessage.getMessage().version()),
                errorMessage ->

                        _logger.error(String.format("Dispatching message has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
    }

    private void updatePublishedVersionAsync(ProcessingDomainEventStreamMessage processingMessage, int retryTimes) {
        _ioHelper.tryAsyncActionRecursively("UpdatePublishedVersionAsync",
                () -> _publishedVersionStore.updatePublishedVersionAsync(getName(), processingMessage.getMessage().aggregateRootTypeName(), processingMessage.getMessage().aggregateRootStringId(), processingMessage.getMessage().version()),
                currentRetryTimes -> updatePublishedVersionAsync(processingMessage, currentRetryTimes),

                result -> {
                    processingMessage.complete();
                },
                () -> String.format("sequence message [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%d]", processingMessage.getMessage().id(), processingMessage.getMessage().getClass().getName(), processingMessage.getMessage().aggregateRootStringId(), processingMessage.getMessage().version()),
                errorMessage ->
                        _logger.error(String.format("Update published version has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
    }
}
