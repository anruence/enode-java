package com.enode.infrastructure.impl;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.IOHelper;
import com.enode.common.logging.ENodeLogger;
import com.enode.infrastructure.IProcessingMessage;
import com.enode.infrastructure.IProcessingMessageHandler;
import com.enode.infrastructure.IPublishedVersionStore;
import com.enode.infrastructure.ISequenceMessage;
import com.enode.infrastructure.ISequenceProcessingMessage;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractSequenceProcessingMessageHandler<X extends IProcessingMessage<X, Y> & ISequenceProcessingMessage, Y extends ISequenceMessage> implements IProcessingMessageHandler<X, Y> {
    private static final Logger _logger = ENodeLogger.getLog();

    private final IPublishedVersionStore _publishedVersionStore;
    private final IOHelper _ioHelper;

    public abstract String getName();

    @Inject
    public AbstractSequenceProcessingMessageHandler(IPublishedVersionStore publishedVersionStore, IOHelper ioHelper) {
        _publishedVersionStore = publishedVersionStore;
        _ioHelper = ioHelper;
    }

    protected abstract CompletableFuture<AsyncTaskResult> dispatchProcessingMessageAsync(X processingMessage);

    @Override
    public void handleAsync(X processingMessage) {
        handleMessageAsync(processingMessage, 0);
    }

    private void handleMessageAsync(X processingMessage, int retryTimes) {
        Y message = processingMessage.getMessage();

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

    private void doDispatchProcessingMessageAsync(X processingMessage, int retryTimes) {
        _ioHelper.tryAsyncActionRecursively("DispatchProcessingMessageAsync",
                () -> dispatchProcessingMessageAsync(processingMessage),
                currentRetryTimes -> doDispatchProcessingMessageAsync(processingMessage, currentRetryTimes),
                result -> updatePublishedVersionAsync(processingMessage, 0),
                () -> String.format("sequence message [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%d]", processingMessage.getMessage().id(), processingMessage.getMessage().getClass().getName(), processingMessage.getMessage().aggregateRootStringId(), processingMessage.getMessage().version()),
                errorMessage ->

                        _logger.error(String.format("Dispatching message has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
    }

    private void updatePublishedVersionAsync(X processingMessage, int retryTimes) {
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
