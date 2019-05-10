package com.enode.kafka;

import com.enode.common.io.AsyncTaskResult;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.queue.QueueMessage;
import com.enode.queue.domainevent.DomainEventPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class KafkaDomainEventPublisher extends DomainEventPublisher {

    @Autowired
    protected SendMessageService _sendMessageService;
    private KafkaProducer producer;

    public KafkaProducer getProducer() {
        return producer;
    }

    public void setProducer(KafkaProducer producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage eventStream) {
        return _sendMessageService.sendMessageAsync(producer, buildKafkaMessage(eventStream));
    }

    protected ProducerRecord buildKafkaMessage(DomainEventStreamMessage eventStream) {
        QueueMessage queueMessage = createDomainEventStreamMessage(eventStream);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }

}
