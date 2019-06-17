package com.enodeframework.kafka;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.domainevent.AbstractDomainEventPublisher;
import org.apache.kafka.clients.producer.ProducerRecord;
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
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage eventStream) {
        return SendMessageService.sendMessageAsync(producer, buildKafkaMessage(eventStream));
    }

    protected ProducerRecord<String, String> buildKafkaMessage(DomainEventStreamMessage eventStream) {
        QueueMessage queueMessage = createDomainEventStreamMessage(eventStream);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }

}
