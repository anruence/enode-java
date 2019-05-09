package com.enode.kafka;

import com.enode.common.serializing.IJsonSerializer;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IEventSerializer;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.ProcessingDomainEventStreamMessage;
import com.enode.queue.IMessageContext;
import com.enode.queue.QueueMessage;
import com.enode.queue.SendReplyService;
import com.enode.queue.command.ConsumeStatus;
import com.enode.queue.domainevent.DomainEventConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.Properties;

@Component
public class KafkaDomainEventConsumer extends DomainEventConsumer implements IMessageListener {

    private KafkaConsumer _consumer;

    @Inject
    public KafkaDomainEventConsumer(IJsonSerializer jsonSerializer, IEventSerializer eventSerializer, IMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> processor, SendReplyService sendReplyService) {
        _sendReplyService = sendReplyService;
        _jsonSerializer = jsonSerializer;
        _eventSerializer = eventSerializer;
        _processor = processor;
        _sendEventHandledMessage = true;
    }

    public KafkaDomainEventConsumer initializeQueue(Properties properties) {
        _consumer = new KafkaConsumer(properties);
        return this;
    }

    public KafkaDomainEventConsumer subscribe(List<String> topics) {
        _consumer.subscribe(topics);
        return this;
    }

    @Override
    public KafkaDomainEventConsumer start() {
        super.start();
        new Thread(new KafkaConsumerRunner<>(_consumer, this)).start();
        return this;
    }

    @Override
    public KafkaDomainEventConsumer shutdown() {
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
