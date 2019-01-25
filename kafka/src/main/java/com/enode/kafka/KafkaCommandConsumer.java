package com.enode.kafka;

import com.enode.commanding.ICommandProcessor;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.domain.IAggregateStorage;
import com.enode.domain.IRepository;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.queue.IMessageContext;
import com.enode.queue.QueueMessage;
import com.enode.queue.SendReplyService;
import com.enode.queue.command.CommandConsumer;
import com.enode.queue.command.ConsumeStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Properties;

@Singleton
public class KafkaCommandConsumer extends CommandConsumer implements IMessageListener {

    private KafkaConsumer _consumer;

    @Inject
    public KafkaCommandConsumer(IJsonSerializer jsonSerializer, ITypeNameProvider typeNameProvider, ICommandProcessor commandProcessor, IRepository repository, IAggregateStorage aggregateStorage, SendReplyService sendReplyService) {
        _sendReplyService = sendReplyService;
        _jsonSerializer = jsonSerializer;
        _typeNameProvider = typeNameProvider;
        _processor = commandProcessor;
        _repository = repository;
        _aggregateRootStorage = aggregateStorage;
    }

    public KafkaCommandConsumer initializeQueue(Properties properties) {
        _consumer = new KafkaConsumer(properties);
        return this;
    }

    public KafkaCommandConsumer subscribe(List<String> topics) {
        _consumer.subscribe(topics);
        return this;
    }

    @Override
    public KafkaCommandConsumer start() {
        new Thread(new KafkaConsumerRunner<>(_consumer, this)).start();
        super.start();
        return this;
    }

    @Override
    public KafkaCommandConsumer shutdown() {
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
