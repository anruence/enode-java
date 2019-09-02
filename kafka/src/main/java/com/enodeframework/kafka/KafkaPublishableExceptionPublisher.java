package com.enodeframework.kafka;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.publishableexception.IPublishableException;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.publishableexceptions.AbstractPublishableExceptionPublisher;
import org.apache.kafka.clients.producer.ProducerRecord;
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
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException exception) {
        return SendMessageService.sendMessageAsync(producer, buildKafkaMessage(exception));
    }

    protected ProducerRecord<String, String> buildKafkaMessage(IPublishableException exception) {
        QueueMessage queueMessage = createExceptionMessage(exception);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }
}
