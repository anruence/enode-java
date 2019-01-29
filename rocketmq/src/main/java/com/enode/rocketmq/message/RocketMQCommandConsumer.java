package com.enode.rocketmq.message;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.commanding.ICommandProcessor;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.domain.IAggregateStorage;
import com.enode.domain.IRepository;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.queue.QueueMessage;
import com.enode.queue.SendReplyService;
import com.enode.queue.command.CommandConsumer;
import com.enode.rocketmq.client.Consumer;
import com.enode.rocketmq.client.RocketMQFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Properties;

@Singleton
public class RocketMQCommandConsumer extends CommandConsumer implements MessageListenerConcurrently {

    private Consumer _consumer;

    private RocketMQFactory _mqFactory;

    @Inject
    public RocketMQCommandConsumer(RocketMQFactory mqFactory, IJsonSerializer jsonSerializer, ITypeNameProvider typeNameProvider, ICommandProcessor commandProcessor, IRepository repository, IAggregateStorage aggregateStorage, SendReplyService sendReplyService) {
        _sendReplyService = sendReplyService;
        _jsonSerializer = jsonSerializer;
        _typeNameProvider = typeNameProvider;
        _processor = commandProcessor;
        _repository = repository;
        _aggregateRootStorage = aggregateStorage;
        _mqFactory = mqFactory;

    }

    public RocketMQCommandConsumer initializeQueue(Properties properties) {
        _consumer = _mqFactory.createPushConsumer(properties);
        _consumer.registerMessageListener(this::consumeMessage);
        return this;
    }

    public RocketMQCommandConsumer subscribe(String topic, String subExpression) {
        _consumer.subscribe(topic, subExpression);
        return this;
    }

    @Override
    public RocketMQCommandConsumer start() {
        super.start();
        _consumer.start();
        return this;
    }

    @Override
    public RocketMQCommandConsumer shutdown() {
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
