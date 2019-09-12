package org.enodeframework.queue.applicationmessage;

import org.enodeframework.applicationmessage.IApplicationMessage;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.enodeframework.messaging.IMessageDispatcher;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractApplicationMessageListener implements IMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractApplicationMessageListener.class);
    @Autowired
    protected ITypeNameProvider typeNameProvider;
    @Autowired
    protected IMessageDispatcher messageDispatcher;

    public AbstractApplicationMessageListener setTypeNameProvider(ITypeNameProvider typeNameProvider) {
        this.typeNameProvider = typeNameProvider;
        return this;
    }

    public AbstractApplicationMessageListener setApplicationMessageProcessor(IMessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
        return this;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        String msg = queueMessage.getBody();
        ApplicationDataMessage appDataMessage = JsonTool.deserialize(msg, ApplicationDataMessage.class);
        Class applicationMessageType = typeNameProvider.getType(appDataMessage.getApplicationMessageType());
        IApplicationMessage message = (IApplicationMessage) JsonTool.deserialize(appDataMessage.getApplicationMessageData(), applicationMessageType);
        if (logger.isDebugEnabled()) {
            logger.debug("ENode application message received, messageId: {}, messageType: {}", message.getId(), message.getClass().getName());
        }
        messageDispatcher.dispatchMessageAsync(message).thenAccept(x -> {
            context.onMessageHandled(queueMessage);
        });
    }
}
