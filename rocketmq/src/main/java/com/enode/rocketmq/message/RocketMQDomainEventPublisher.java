package com.enode.rocketmq.message;

import com.alibaba.rocketmq.common.message.Message;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.eventing.IEventSerializer;
import com.enode.queue.ITopicProvider;
import com.enode.queue.QueueMessage;
import com.enode.queue.domainevent.DomainEventPublisher;
import com.enode.rocketmq.client.Producer;
import com.enode.rocketmq.client.RocketMQFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Singleton
public class RocketMQDomainEventPublisher extends DomainEventPublisher {

    private SendRocketMQService _sendMessageService;

    private Producer _producer;

    private RocketMQFactory _mqFactory;

    @Inject
    public RocketMQDomainEventPublisher(RocketMQFactory mqFactory, IJsonSerializer jsonSerializer, ITopicProvider<IDomainEvent> eventITopicProvider, IEventSerializer eventSerializer, SendRocketMQService sendMessageService) {
        _jsonSerializer = jsonSerializer;
        _eventTopicProvider = eventITopicProvider;
        _eventSerializer = eventSerializer;
        _sendMessageService = sendMessageService;
        _mqFactory = mqFactory;
    }

    public RocketMQDomainEventPublisher initializeQueue(Properties properties) {
        _producer = _mqFactory.createProducer(properties);
        return this;
    }

    @Override
    public RocketMQDomainEventPublisher start() {
        super.start();
        _producer.start();
        return this;
    }

    @Override
    public RocketMQDomainEventPublisher shutdown() {
        _producer.shutdown();
        super.shutdown();
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage eventStream) {
        QueueMessage queueMessage = createDomainEventStreamMessage(eventStream);
        Message message = RocketMQTool.covertToProducerRecord(queueMessage);
        return _sendMessageService.sendMessageAsync(_producer, message, queueMessage.getRouteKey());
    }

}
