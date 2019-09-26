package org.enodeframework.queue.publishableexceptions;

import org.enodeframework.common.exception.InvalidOperationException;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.enodeframework.messaging.IMessageDispatcher;
import org.enodeframework.publishableexception.IPublishableException;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
            throw new InvalidOperationException(e);
        }
        exception.setId(exceptionMessage.getUniqueId());
        exception.setTimestamp(exceptionMessage.getTimestamp());
        exception.setItems(exceptionMessage.getItems());
        exception.restoreFrom(exceptionMessage.getSerializableInfo());
        if (logger.isDebugEnabled()) {
            logger.debug("ENode exception message received, messageId: {}", exceptionMessage.getUniqueId());
        }
        messageDispatcher.dispatchMessageAsync(exception).thenAccept(x -> {
            context.onMessageHandled(queueMessage);
        });
    }
}
