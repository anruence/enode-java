package com.enode.kafka;

import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.IPublishableException;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.ProcessingPublishableExceptionMessage;
import com.enode.queue.IMessageContext;
import com.enode.queue.QueueMessage;
import com.enode.queue.command.ConsumeStatus;
import com.enode.queue.publishableexceptions.PublishableExceptionConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Properties;

@Singleton
public class KafkaPublishableExceptionConsumer extends PublishableExceptionConsumer implements IMessageListener {

    private KafkaConsumer _consumer;

    @Inject
    public KafkaPublishableExceptionConsumer(IJsonSerializer jsonSerializer, ITypeNameProvider typeNameProvider, IMessageProcessor<ProcessingPublishableExceptionMessage, IPublishableException> publishableExceptionProcessor) {
        _jsonSerializer = jsonSerializer;
        _typeNameProvider = typeNameProvider;
        _publishableExceptionProcessor = publishableExceptionProcessor;
    }

    public KafkaPublishableExceptionConsumer initializeQueue(Properties properties) {
        _consumer = new KafkaConsumer(properties);
        return this;
    }

    public KafkaPublishableExceptionConsumer subscribe(List<String> topics) {
        _consumer.subscribe(topics);
        return this;
    }

    @Override
    public KafkaPublishableExceptionConsumer start() {
        super.start();
        new Thread(new KafkaConsumerRunner<>(_consumer, this)).start();
        return this;
    }

    @Override
    public KafkaPublishableExceptionConsumer shutdown() {
        _consumer.close();
        super.shutdown();
        return this;
    }

    @Override
    public ConsumeStatus receiveMessage(ConsumerRecord message, IMessageContext context) {
        QueueMessage queueMessage = KafkaTool.covertToQueueMessage(message);
        handle(queueMessage, context);
        return ConsumeStatus.CONSUME_SUCCESS;
    }

}
