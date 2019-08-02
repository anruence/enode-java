package com.enodeframework.infrastructure.impl;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.IOHelper;
import com.enodeframework.common.io.Task;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.infrastructure.IMessage;
import com.enodeframework.infrastructure.IMessageDispatcher;
import com.enodeframework.infrastructure.IProcessingMessage;
import com.enodeframework.infrastructure.IProcessingMessageHandler;
import com.enodeframework.infrastructure.IPublishedVersionStore;
import com.enodeframework.infrastructure.ProcessingDomainEventStreamMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DefaultProcessingMessageHandler<X extends IProcessingMessage<X, Y>, Y extends IMessage> implements IProcessingMessageHandler<X, Y> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessingMessageHandler.class);
    private String domainEventStreamMessageHandlerName = "DefaultEventProcessor";
    @Autowired
    private IMessageDispatcher dispatcher;
    @Autowired
    private IPublishedVersionStore publishedVersionStore;

    public DefaultProcessingMessageHandler<X, Y> setDispatcher(IMessageDispatcher dispatcher) {
        this.dispatcher = dispatcher;
        return this;
    }

    public DefaultProcessingMessageHandler<X, Y> setPublishedVersionStore(IPublishedVersionStore publishedVersionStore) {
        this.publishedVersionStore = publishedVersionStore;
        return this;
    }

    @Override
    public CompletableFuture<Void> handleAsync(X processingMessage) {
        if (processingMessage instanceof ProcessingDomainEventStreamMessage) {
            return handleMessageAsync((ProcessingDomainEventStreamMessage) processingMessage, 0);
        }
        return dispatcher.dispatchMessageAsync(processingMessage.getMessage()).thenAccept(r -> processingMessage.complete());
    }

    public String getName() {
        return domainEventStreamMessageHandlerName;
    }

    private CompletableFuture<AsyncTaskResult> dispatchProcessingMessageAsync(ProcessingDomainEventStreamMessage processingMessage) {
        return dispatcher.dispatchMessagesAsync(processingMessage.getMessage().getEvents());
    }

    private CompletableFuture<Void> handleMessageAsync(ProcessingDomainEventStreamMessage processingMessage, int retryTimes) {
        DomainEventStreamMessage message = processingMessage.getMessage();
        IOHelper.tryAsyncActionRecursively("GetPublishedVersionAsync",
                () -> publishedVersionStore.getPublishedVersionAsync(getName(), message.getAggregateRootTypeName(), message.getAggregateRootStringId()),
                result ->
                {
                    int publishedVersion = result.getData();
                    if (publishedVersion + 1 == message.getVersion()) {
                        doDispatchProcessingMessageAsync(processingMessage, 0);
                    } else if (publishedVersion + 1 < message.getVersion()) {
                        logger.info("The sequence message cannot be process now as the version is not the next version, it will be handle later. contextInfo [aggregateRootId={},lastPublishedVersion={},messageVersion={}]", message.getAggregateRootStringId(), publishedVersion, message.getVersion());
                        processingMessage.addToWaitingList();
                    } else {
                        processingMessage.complete();
                    }
                },
                () -> String.format("sequence message [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%s]", message.getId(), message.getClass().getName(), message.getAggregateRootStringId(), message.getVersion()),
                errorMessage ->
                        logger.error(String.format("Get published version has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
        return Task.completedTask;
    }

    private void doDispatchProcessingMessageAsync(ProcessingDomainEventStreamMessage processingMessage, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("DispatchProcessingMessageAsync",
                () -> dispatchProcessingMessageAsync(processingMessage),
                result -> updatePublishedVersionAsync(processingMessage, 0),
                () -> String.format("sequence message [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%d]", processingMessage.getMessage().getId(), processingMessage.getMessage().getClass().getName(), processingMessage.getMessage().getAggregateRootStringId(), processingMessage.getMessage().getVersion()),
                errorMessage -> logger.error("Dispatching message has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage),
                retryTimes, true);
    }

    private void updatePublishedVersionAsync(ProcessingDomainEventStreamMessage processingMessage, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("UpdatePublishedVersionAsync",
                () -> publishedVersionStore.updatePublishedVersionAsync(getName(), processingMessage.getMessage().getAggregateRootTypeName(), processingMessage.getMessage().getAggregateRootStringId(), processingMessage.getMessage().getVersion()),
                result -> processingMessage.complete(),
                () -> String.format("sequence message [messageId:%s, messageType:%s, aggregateRootId:%s, aggregateRootVersion:%d]", processingMessage.getMessage().getId(), processingMessage.getMessage().getClass().getName(), processingMessage.getMessage().getAggregateRootStringId(), processingMessage.getMessage().getVersion()),
                errorMessage ->
                        logger.error(String.format("Update published version has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
    }
}
