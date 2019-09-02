package com.enodeframework.queue.publishableexceptions;

import com.enodeframework.common.exception.ENodeRuntimeException;
import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.messaging.IMessageDispatcher;
import com.enodeframework.publishableexception.IPublishableException;
import com.enodeframework.infrastructure.ITypeNameProvider;
import com.enodeframework.publishableexception.ProcessingPublishableExceptionMessage;
import com.enodeframework.messaging.impl.DefaultMessageProcessContext;
import com.enodeframework.queue.IMessageContext;
import com.enodeframework.queue.IMessageHandler;
import com.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public abstract class AbstractPublishableExceptionListener implements IMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractPublishableExceptionListener.class);
    @Autowired
    protected ITypeNameProvider typeNameProvider;

    @Autowired
    private IMessageDispatcher messageDispatcher;

    public AbstractPublishableExceptionListener setTypeNameProvider(ITypeNameProvider typeNameProvider) {
        this.typeNameProvider = typeNameProvider;
        return this;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        PublishableExceptionMessage exceptionMessage = JsonTool.deserialize(queueMessage.getBody(), PublishableExceptionMessage.class);
        Class exceptionType = typeNameProvider.getType(exceptionMessage.getExceptionType());
        IPublishableException exception;
        try {
            exception = (IPublishableException) exceptionType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ENodeRuntimeException(e);
        }
        exception.setId(exceptionMessage.getUniqueId());
        exception.setTimestamp(exceptionMessage.getTimestamp());
        exception.restoreFrom(exceptionMessage.getSerializableInfo());
        DefaultMessageProcessContext processContext = new DefaultMessageProcessContext(queueMessage, context);
        ProcessingPublishableExceptionMessage processingMessage = new ProcessingPublishableExceptionMessage(exception, processContext);
        if (logger.isDebugEnabled()) {
            logger.debug("ENode exception message received, messageId: {}, aggregateRootId: {}, aggregateRootType: {}", exceptionMessage.getUniqueId(), exceptionMessage.getAggregateRootId(), exceptionMessage.getAggregateRootTypeName());
        }
        CompletableFuture.runAsync(() -> {
            messageDispatcher.dispatchMessageAsync(processingMessage.getMessage()).thenAccept(x -> {
                processingMessage.complete();
            });
        });
    }
}
