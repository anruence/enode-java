package org.enodeframework.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.publishableexceptions.AbstractPublishableExceptionPublisher;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class KafkaPublishableExceptionPublisher extends AbstractPublishableExceptionPublisher {
    private KafkaTemplate<String, String> producer;

    public KafkaTemplate<String, String> getProducer() {
        return producer;
    }

    public void setProducer(KafkaTemplate<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<Void> publishAsync(IDomainException exception) {
        return SendMessageService.sendMessageAsync(producer, buildKafkaMessage(exception));
    }

    protected ProducerRecord<String, String> buildKafkaMessage(IDomainException exception) {
        QueueMessage queueMessage = createExceptionMessage(exception);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }
}
