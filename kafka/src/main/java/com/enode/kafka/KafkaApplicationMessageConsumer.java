package com.enode.kafka;

import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.infrastructure.ProcessingApplicationMessage;
import com.enode.queue.IMessageContext;
import com.enode.queue.QueueMessage;
import com.enode.queue.applicationmessage.ApplicationMessageConsumer;
import com.enode.queue.command.ConsumeStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Properties;

@Singleton
public class KafkaApplicationMessageConsumer extends ApplicationMessageConsumer implements IMessageListener {

    private KafkaConsumer _consumer;

    @Inject
    public KafkaApplicationMessageConsumer(IJsonSerializer jsonSerializer, ITypeNameProvider typeNameProvider, IMessageProcessor<ProcessingApplicationMessage, IApplicationMessage> processor) {
        _jsonSerializer = jsonSerializer;
        _typeNameProvider = typeNameProvider;
        _processor = processor;
    }


    public KafkaApplicationMessageConsumer initializeQueue(Properties properties) {
        _consumer = new KafkaConsumer(properties);
        return this;
    }

    public KafkaApplicationMessageConsumer subscribe(List<String> topics) {
        _consumer.subscribe(topics);
        return this;
    }

    @Override
    public KafkaApplicationMessageConsumer start() {
        super.start();
        new Thread(new KafkaConsumerRunner<>(_consumer, this)).start();
        return this;
    }

    @Override
    public KafkaApplicationMessageConsumer shutdown() {
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
