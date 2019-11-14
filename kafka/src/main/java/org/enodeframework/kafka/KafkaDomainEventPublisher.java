package org.enodeframework.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.domainevent.AbstractDomainEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class KafkaDomainEventPublisher extends AbstractDomainEventPublisher {
    private KafkaTemplate<String, String> producer;

    public KafkaTemplate<String, String> getProducer() {
        return producer;
    }

    public void setProducer(KafkaTemplate<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<Void> publishAsync(DomainEventStreamMessage eventStream) {
        return SendMessageService.sendMessageAsync(producer, buildKafkaMessage(eventStream));
    }

    protected ProducerRecord<String, String> buildKafkaMessage(DomainEventStreamMessage eventStream) {
        QueueMessage queueMessage = createDomainEventStreamMessage(eventStream);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }
}
