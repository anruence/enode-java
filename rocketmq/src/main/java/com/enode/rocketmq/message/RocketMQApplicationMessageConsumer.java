package com.enode.rocketmq.message;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.ProcessingApplicationMessage;
import com.enode.queue.QueueMessage;
import com.enode.queue.applicationmessage.ApplicationMessageConsumer;
import com.enode.rocketmq.client.Consumer;
import com.enode.rocketmq.client.RocketMQFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Properties;

@Singleton
public class RocketMQApplicationMessageConsumer extends ApplicationMessageConsumer implements MessageListenerConcurrently {

    private Consumer _consumer;

    private RocketMQFactory _mqFactory;

    @Inject
    public RocketMQApplicationMessageConsumer(RocketMQFactory mqFactory, IJsonSerializer jsonSerializer, ITypeNameProvider typeNameProvider, IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> processor) {
        _jsonSerializer = jsonSerializer;
        _typeNameProvider = typeNameProvider;
        _processor = processor;
        _mqFactory = mqFactory;
    }

    public RocketMQApplicationMessageConsumer initializeQueue(Properties properties) {
        _consumer = _mqFactory.createPushConsumer(properties);
        _consumer.registerMessageListener(this::consumeMessage);
        return this;
    }

    public RocketMQApplicationMessageConsumer subscribe(String topic, String subExpression) {
        _consumer.subscribe(topic, subExpression);
        return this;
    }

    @Override
    public RocketMQApplicationMessageConsumer start() {
        super.start();
        _consumer.start();
        return this;
    }

    @Override
    public RocketMQApplicationMessageConsumer shutdown() {
        _consumer.shutdown();
        super.shutdown();
        return this;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        QueueMessage queueMessage = RocketMQTool.covertToQueueMessage(msgs);
        handle(queueMessage, message -> {
            return;
        });
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }

}
