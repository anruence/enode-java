package com.enodeframework.queue.publishableexceptions;

import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.infrastructure.IMessageProcessor;
import com.enodeframework.infrastructure.IPublishableException;
import com.enodeframework.infrastructure.ISequenceMessage;
import com.enodeframework.infrastructure.ITypeNameProvider;
import com.enodeframework.infrastructure.ProcessingPublishableExceptionMessage;
import com.enodeframework.infrastructure.WrappedRuntimeException;
import com.enodeframework.infrastructure.impl.DefaultMessageProcessContext;
import com.enodeframework.queue.IMessageContext;
import com.enodeframework.queue.IMessageHandler;
import com.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractPublishableExceptionListener implements IMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPublishableExceptionListener.class);

    @Autowired
    protected ITypeNameProvider typeNameProvider;

    @Autowired
    protected IMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException> publishableExceptionProcessor;

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        PublishableExceptionMessage exceptionMessage = JsonTool.deserialize(queueMessage.getBody(), PublishableExceptionMessage.class);
        Class exceptionType = typeNameProvider.getType(exceptionMessage.getExceptionType());
        IPublishableException exception;
        try {
            exception = (IPublishableException) exceptionType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
        exception.setId(exceptionMessage.getUniqueId());
        exception.setTimestamp(exceptionMessage.getTimestamp());
        exception.restoreFrom(exceptionMessage.getSerializableInfo());

        if (exception instanceof ISequenceMessage) {
            ISequenceMessage sequenceMessage = (ISequenceMessage) exception;
            sequenceMessage.setAggregateRootTypeName(exceptionMessage.getAggregateRootTypeName());
            sequenceMessage.setAggregateRootStringId(exceptionMessage.getAggregateRootId());
        }

        DefaultMessageProcessContext processContext = new DefaultMessageProcessContext(queueMessage, context);
        ProcessingPublishableExceptionMessage processingMessage = new ProcessingPublishableExceptionMessage(exception, processContext);
        if (logger.isDebugEnabled()) {
            logger.info("ENode exception message received, messageId: {}, aggregateRootId: {}, aggregateRootType: {}", exceptionMessage.getUniqueId(), exceptionMessage.getAggregateRootId(), exceptionMessage.getAggregateRootTypeName());
        }
        publishableExceptionProcessor.process(processingMessage);
    }
}
