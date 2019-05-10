package com.enode.kafka;

import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IPublishableException;
import com.enode.queue.QueueMessage;
import com.enode.queue.publishableexceptions.PublishableExceptionPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class KafkaPublishableExceptionPublisher extends PublishableExceptionPublisher {

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
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException exception) {
        return _sendMessageService.sendMessageAsync(producer, buildKafkaMessage(exception));
    }

    protected ProducerRecord buildKafkaMessage(IPublishableException exception) {
        QueueMessage queueMessage = createExecptionMessage(exception);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }
}
