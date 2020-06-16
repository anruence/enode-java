package org.enodeframework.queue.publishableexceptions;

import org.enodeframework.common.exception.InvalidOperationException;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.enodeframework.messaging.IMessageDispatcher;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultPublishableExceptionListener implements IMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPublishableExceptionListener.class);

    private final ITypeNameProvider typeNameProvider;

    private final IMessageDispatcher messageDispatcher;

    public DefaultPublishableExceptionListener(ITypeNameProvider typeNameProvider, IMessageDispatcher messageDispatcher) {
        this.typeNameProvider = typeNameProvider;
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        PublishableExceptionMessage exceptionMessage = JsonTool.deserialize(queueMessage.getBody(), PublishableExceptionMessage.class);
        Class<?> exceptionType = typeNameProvider.getType(exceptionMessage.getExceptionType());
        IDomainException exception;
        try {
            exception = (IDomainException) exceptionType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new InvalidOperationException(e);
        }
        exception.setId(exceptionMessage.getUniqueId());
        exception.setTimestamp(exceptionMessage.getTimestamp());
        exception.setItems(exceptionMessage.getItems());
        exception.restoreFrom(exceptionMessage.getSerializableInfo());
        if (logger.isDebugEnabled()) {
            logger.debug("Enode exception message received, messageId: {}", exceptionMessage.getUniqueId());
        }
        messageDispatcher.dispatchMessageAsync(exception).thenAccept(x -> {
            context.onMessageHandled(queueMessage);
        });
    }
}
