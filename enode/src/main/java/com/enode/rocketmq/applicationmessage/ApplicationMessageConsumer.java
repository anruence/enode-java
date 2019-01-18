package com.enode.rocketmq.applicationmessage;

import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.utilities.BitConverter;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.ProcessingApplicationMessage;
import com.enode.message.ApplicationDataMessage;
import com.enode.rocketmq.ITopicProvider;
import com.enode.rocketmq.RocketMQConsumer;
import com.enode.rocketmq.RocketMQMessageHandler;
import com.enode.rocketmq.RocketMQProcessContext;
import com.enode.rocketmq.TopicTagData;
import com.enode.rocketmq.consumer.listener.CompletableConsumeConcurrentlyContext;
import org.slf4j.Logger;

import javax.inject.Inject;

public class ApplicationMessageConsumer {

    private static final Logger _logger = ENodeLogger.getLog();

    private final RocketMQConsumer _consumer;
    private final IJsonSerializer _jsonSerializer;
    private final ITopicProvider<IApplicationMessage> _messageTopicProvider;
    private final ITypeNameProvider _typeNameProvider;
    private final IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> _processor;

    @Inject
    public ApplicationMessageConsumer(RocketMQConsumer consumer, IJsonSerializer jsonSerializer,
                                      ITopicProvider<IApplicationMessage> messageITopicProvider, ITypeNameProvider typeNameProvider,
                                      IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> processor) {
        _consumer = consumer;
        _jsonSerializer = jsonSerializer;
        _messageTopicProvider = messageITopicProvider;
        _typeNameProvider = typeNameProvider;
        _processor = processor;
    }

    public ApplicationMessageConsumer start() {
        _consumer.registerMessageHandler(new RocketMQMessageHandler() {
            @Override
            public boolean isMatched(TopicTagData topicTagData) {
                return _messageTopicProvider.getAllSubscribeTopics().contains(topicTagData);
            }

            @Override
            public void handle(Object msg, Object context) {
                MessageExt messageExt = (MessageExt) msg;
                CompletableConsumeConcurrentlyContext concurrentlyContext = (CompletableConsumeConcurrentlyContext) context;
                handle(messageExt, concurrentlyContext);
            }

            public void handle(MessageExt message, CompletableConsumeConcurrentlyContext context) {
                ApplicationMessageConsumer.this.handle(message, context);
            }
        });
        return this;
    }

    public ApplicationMessageConsumer shutdown() {
        return this;
    }

    void handle(final MessageExt msg, final CompletableConsumeConcurrentlyContext context) {
        ApplicationDataMessage appDataMessage = _jsonSerializer.deserialize(BitConverter.toString(msg.getBody()), ApplicationDataMessage.class);
        Class applicationMessageType;

        try {
            applicationMessageType = _typeNameProvider.getType(appDataMessage.getApplicationMessageType());
        } catch (Exception e) {
            _logger.warn("Consume applicatio message exception:", e);
            context.onMessageHandled();
            return;
        }

        IApplicationMessage message = (IApplicationMessage) _jsonSerializer.deserialize(appDataMessage.getApplicationMessageData(), applicationMessageType);
        RocketMQProcessContext processContext = new RocketMQProcessContext(msg, context);
        ProcessingApplicationMessage processingMessage = new ProcessingApplicationMessage(message, processContext);
        _logger.info("ENode application message received, messageId: {}, routingKey: {}", message.id(), message.getRoutingKey());
        _processor.process(processingMessage);
//        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

    public RocketMQConsumer getConsumer() {
        return _consumer;
    }
}
