package com.enode.kafka;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.eventing.IEventSerializer;
import com.enode.queue.ITopicProvider;
import com.enode.queue.QueueMessage;
import com.enode.queue.domainevent.DomainEventPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Singleton
public class KafkaDomainEventPublisher extends DomainEventPublisher {

    private SendMessageService _sendMessageService;

    private KafkaProducer _producer;

    @Inject
    public KafkaDomainEventPublisher(IJsonSerializer jsonSerializer, ITopicProvider<IDomainEvent> eventITopicProvider, IEventSerializer eventSerializer, SendMessageService sendMessageService) {
        _jsonSerializer = jsonSerializer;
        _eventTopicProvider = eventITopicProvider;
        _eventSerializer = eventSerializer;
        _sendMessageService = sendMessageService;
    }

    public KafkaDomainEventPublisher initializeQueue(Properties properties) {
        _producer = new KafkaProducer(properties);
        return this;
    }

    @Override
    public KafkaDomainEventPublisher start() {
        return this;
    }

    @Override
    public KafkaDomainEventPublisher shutdown() {
        _producer.close();
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage eventStream) {
        return _sendMessageService.sendMessageAsync(_producer, buildKafkaMessage(eventStream));
    }

    protected ProducerRecord buildKafkaMessage(DomainEventStreamMessage eventStream) {
        QueueMessage queueMessage = createDomainEventStreamMessage(eventStream);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }

}
