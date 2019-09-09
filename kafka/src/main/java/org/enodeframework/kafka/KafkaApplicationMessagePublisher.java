package org.enodeframework.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.enodeframework.applicationmessage.IApplicationMessage;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.applicationmessage.AbstractApplicationMessagePublisher;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class KafkaApplicationMessagePublisher extends AbstractApplicationMessagePublisher {
    private KafkaTemplate<String, String> producer;

    public KafkaTemplate<String, String> getProducer() {
        return producer;
    }

    public void setProducer(KafkaTemplate<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        return SendMessageService.sendMessageAsync(producer, buildKafkaMessage(message));
    }

    protected ProducerRecord<String, String> buildKafkaMessage(IApplicationMessage message) {
        QueueMessage queueMessage = createApplicationMessage(message);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }
}
