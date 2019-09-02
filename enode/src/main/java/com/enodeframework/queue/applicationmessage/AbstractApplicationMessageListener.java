package com.enodeframework.queue.applicationmessage;

import com.enodeframework.applicationmessage.IApplicationMessage;
import com.enodeframework.applicationmessage.ProcessingApplicationMessage;
import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.messaging.IMessageDispatcher;
import com.enodeframework.infrastructure.ITypeNameProvider;
import com.enodeframework.messaging.impl.DefaultMessageProcessContext;
import com.enodeframework.queue.IMessageContext;
import com.enodeframework.queue.IMessageHandler;
import com.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

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
        DefaultMessageProcessContext processContext = new DefaultMessageProcessContext(queueMessage, context);
        ProcessingApplicationMessage processingMessage = new ProcessingApplicationMessage(message, processContext);
        if (logger.isDebugEnabled()) {
            logger.debug("ENode application message received, messageId: {}", message.getId());
        }
        CompletableFuture.runAsync(() -> {
            messageDispatcher.dispatchMessageAsync(processingMessage.getMessage()).thenAccept(x -> {
                processingMessage.complete();
            });
        });
    }
}
