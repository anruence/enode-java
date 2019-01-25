package com.enode.kafka;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.queue.ITopicProvider;
import com.enode.queue.QueueMessage;
import com.enode.queue.applicationmessage.ApplicationMessagePublisher;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Singleton
public class KafkaApplicationMessagePublisher extends ApplicationMessagePublisher {

    private SendMessageService _sendMessageService;

    private KafkaProducer _producer;

    @Inject
    public KafkaApplicationMessagePublisher(IJsonSerializer jsonSerializer, ITopicProvider<IApplicationMessage> messageTopicProvider, SendMessageService sendMessageService) {
        _jsonSerializer = jsonSerializer;
        _messageTopicProvider = messageTopicProvider;
        _sendMessageService = sendMessageService;
    }

    public KafkaApplicationMessagePublisher initializeQueue(Properties properties) {
        _producer = new KafkaProducer(properties);
        return this;
    }

    @Override
    public KafkaApplicationMessagePublisher start() {
        return this;
    }

    @Override
    public KafkaApplicationMessagePublisher shutdown() {
        _producer.close();
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        return _sendMessageService.sendMessageAsync(_producer, buildKafkaMessage(message));
    }

    protected ProducerRecord buildKafkaMessage(IApplicationMessage message) {
        QueueMessage queueMessage = createApplicationMessage(message);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }
}
