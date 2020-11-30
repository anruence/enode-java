package org.enodeframework.queue.publishableexceptions;

import org.enodeframework.common.exception.MessageInstanceCreateException;
import org.enodeframework.common.serializing.ISerializeService;
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

    private final ISerializeService serializeService;

    public DefaultPublishableExceptionListener(ITypeNameProvider typeNameProvider, IMessageDispatcher messageDispatcher, ISerializeService serializeService) {
        this.typeNameProvider = typeNameProvider;
        this.messageDispatcher = messageDispatcher;
        this.serializeService = serializeService;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        logger.info("Received domain exception message: {}", serializeService.serialize(queueMessage));
        PublishableExceptionMessage exceptionMessage = serializeService.deserialize(queueMessage.getBody(), PublishableExceptionMessage.class);
        Class<?> exceptionType = typeNameProvider.getType(exceptionMessage.getExceptionType());
        IDomainException exception;
        try {
            exception = (IDomainException) exceptionType.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new MessageInstanceCreateException(e);
        }
        exception.setId(exceptionMessage.getUniqueId());
        exception.setTimestamp(exceptionMessage.getTimestamp());
        exception.setItems(exceptionMessage.getItems());
        exception.restoreFrom(exceptionMessage.getSerializableInfo());
        messageDispatcher.dispatchMessageAsync(exception).whenComplete((x, y) -> {
            context.onMessageHandled(queueMessage);
        });
    }
}
