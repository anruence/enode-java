package com.enode.queue.applicationmessage;

import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.ProcessingApplicationMessage;
import com.enode.infrastructure.impl.DefaultMessageProcessContext;
import com.enode.queue.IMessageContext;
import com.enode.queue.IMessageHandler;
import com.enode.queue.QueueMessage;
import org.slf4j.Logger;

public class ApplicationMessageConsumer implements IMessageHandler {

    private static final Logger _logger = ENodeLogger.getLog();

    protected String defaultMessageConsumerGroup = "ApplicationMessageConsumerGroup";

    protected IJsonSerializer _jsonSerializer;

    protected ITypeNameProvider _typeNameProvider;

    protected IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> _processor;

    public ApplicationMessageConsumer start() {
        return this;
    }

    public ApplicationMessageConsumer shutdown() {
        return this;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        String msg = queueMessage.getBody();
        ApplicationDataMessage appDataMessage = _jsonSerializer.deserialize(msg, ApplicationDataMessage.class);
        Class applicationMessageType;

        try {
            applicationMessageType = _typeNameProvider.getType(appDataMessage.getApplicationMessageType());
        } catch (Exception e) {
            _logger.warn("Consume application message exception:", e);
            return;
        }
        IApplicationMessage message = (IApplicationMessage) _jsonSerializer.deserialize(appDataMessage.getApplicationMessageData(), applicationMessageType);
        DefaultMessageProcessContext processContext = new DefaultMessageProcessContext(queueMessage, context);
        ProcessingApplicationMessage processingMessage = new ProcessingApplicationMessage(message, processContext);
        _logger.info("ENode application message received, messageId: {}, routingKey: {}", message.id(), message.getRoutingKey());
        _processor.process(processingMessage);
    }
}
