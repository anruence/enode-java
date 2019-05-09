package com.enode.kafka;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IPublishableException;
import com.enode.queue.ITopicProvider;
import com.enode.queue.QueueMessage;
import com.enode.queue.publishableexceptions.PublishableExceptionPublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Component
public class KafkaPublishableExceptionPublisher extends PublishableExceptionPublisher {

    protected KafkaProducer _producer;

    protected SendMessageService _sendMessageService;

    @Inject
    public KafkaPublishableExceptionPublisher(IJsonSerializer jsonSerializer, ITopicProvider<IPublishableException> exceptionTopicProvider, SendMessageService sendMessageService) {
        _jsonSerializer = jsonSerializer;
        _exceptionTopicProvider = exceptionTopicProvider;
        _sendMessageService = sendMessageService;
    }

    public KafkaPublishableExceptionPublisher initializeQueue(Properties properties) {
        _producer = new KafkaProducer(properties);
        return this;
    }

    @Override
    public KafkaPublishableExceptionPublisher start() {
        return this;
    }

    @Override
    public KafkaPublishableExceptionPublisher shutdown() {
        _producer.close();
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException exception) {
        return _sendMessageService.sendMessageAsync(_producer, buildKafkaMessage(exception));
    }

    protected ProducerRecord buildKafkaMessage(IPublishableException exception) {
        QueueMessage queueMessage = createExecptionMessage(exception);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }
}
