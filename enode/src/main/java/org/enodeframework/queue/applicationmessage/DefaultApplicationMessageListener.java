package org.enodeframework.queue.applicationmessage;

import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessageDispatcher;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultApplicationMessageListener implements IMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultApplicationMessageListener.class);
    private final ITypeNameProvider typeNameProvider;
    private final IMessageDispatcher messageDispatcher;

    public DefaultApplicationMessageListener(ITypeNameProvider typeNameProvider, IMessageDispatcher messageDispatcher) {
        this.typeNameProvider = typeNameProvider;
        this.messageDispatcher = messageDispatcher;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        String msg = queueMessage.getBody();
        ApplicationDataMessage appDataMessage = JsonTool.deserialize(msg, ApplicationDataMessage.class);
        Class<?> applicationMessageType = typeNameProvider.getType(appDataMessage.getApplicationMessageType());
        IApplicationMessage message = (IApplicationMessage) JsonTool.deserialize(appDataMessage.getApplicationMessageData(), applicationMessageType);
        if (logger.isDebugEnabled()) {
            logger.debug("Enode application message received, messageId: {}, messageType: {}", message.getId(), message.getClass().getName());
        }
        messageDispatcher.dispatchMessageAsync(message).thenAccept(x -> {
            context.onMessageHandled(queueMessage);
        });
    }
}
