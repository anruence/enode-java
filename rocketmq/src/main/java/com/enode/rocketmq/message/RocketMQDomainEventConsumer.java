package com.enode.rocketmq.message;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IEventSerializer;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.ProcessingDomainEventStreamMessage;
import com.enode.queue.QueueMessage;
import com.enode.queue.SendReplyService;
import com.enode.queue.domainevent.DomainEventConsumer;
import com.enode.rocketmq.client.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
public class RocketMQDomainEventConsumer extends DomainEventConsumer implements MessageListenerConcurrently {


    private Consumer _consumer;

    @Inject
    public RocketMQDomainEventConsumer(Consumer consumer, IJsonSerializer jsonSerializer, IEventSerializer eventSerializer, IMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> processor, SendReplyService sendReplyService) {
        _sendReplyService = sendReplyService;
        _jsonSerializer = jsonSerializer;
        _eventSerializer = eventSerializer;
        _processor = processor;
        _sendEventHandledMessage = true;
        _consumer = consumer;
    }

    public RocketMQDomainEventConsumer subscribe(String topic, String subExpression) {
        _consumer.subscribe(topic, subExpression);
        _consumer.registerMessageListener(this::consumeMessage);
        return this;
    }

    @Override
    public RocketMQDomainEventConsumer start() {
        super.start();
        return this;
    }

    @Override
    public RocketMQDomainEventConsumer shutdown() {
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
