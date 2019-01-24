package com.enode.queue.applicationmessage;

import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.ProcessingApplicationMessage;
import com.enode.infrastructure.impl.DefaultMessageProcessContext;
import com.enode.queue.CompletableConsumeConcurrentlyContext;
import com.enode.queue.IMQConsumer;
import com.enode.queue.IMQMessageHandler;
import com.enode.queue.ITopicProvider;
import com.enode.queue.TopicData;
import org.slf4j.Logger;

import javax.inject.Inject;

public class ApplicationMessageConsumer {

    private static final Logger _logger = ENodeLogger.getLog();

    private final IMQConsumer _consumer;
    private final IJsonSerializer _jsonSerializer;
    private final ITopicProvider<IApplicationMessage> _messageTopicProvider;
    private final ITypeNameProvider _typeNameProvider;
    private final IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> _processor;

    @Inject
    public ApplicationMessageConsumer(
            IMQConsumer consumer, IJsonSerializer jsonSerializer,
            ITopicProvider<IApplicationMessage> messageITopicProvider, ITypeNameProvider typeNameProvider,
            IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> processor) {
        _consumer = consumer;
        _jsonSerializer = jsonSerializer;
        _messageTopicProvider = messageITopicProvider;
        _typeNameProvider = typeNameProvider;
        _processor = processor;
    }

    public ApplicationMessageConsumer start() {
        _consumer.registerMessageHandler(new ApplicationMessageHandle());
        return this;
    }

    public ApplicationMessageConsumer shutdown() {
        return this;
    }

    class ApplicationMessageHandle implements IMQMessageHandler {
        @Override
        public boolean isMatched(TopicData topicTagData) {
            return _messageTopicProvider.getAllSubscribeTopics().contains(topicTagData);
        }

        @Override
        public void handle(String msg, CompletableConsumeConcurrentlyContext context) {
            ApplicationDataMessage appDataMessage = _jsonSerializer.deserialize(msg, ApplicationDataMessage.class);
            Class applicationMessageType;

            try {
                applicationMessageType = _typeNameProvider.getType(appDataMessage.getApplicationMessageType());
            } catch (Exception e) {
                _logger.warn("Consume application message exception:", e);
                return;
            }
            IApplicationMessage message = (IApplicationMessage) _jsonSerializer.deserialize(appDataMessage.getApplicationMessageData(), applicationMessageType);
            DefaultMessageProcessContext processContext = new DefaultMessageProcessContext(msg, context);
            ProcessingApplicationMessage processingMessage = new ProcessingApplicationMessage(message, processContext);
            _logger.info("ENode application message received, messageId: {}, routingKey: {}", message.id(), message.getRoutingKey());
            _processor.process(processingMessage);
        }
    }

}
