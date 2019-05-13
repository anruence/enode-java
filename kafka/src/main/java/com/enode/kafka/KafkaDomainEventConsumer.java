package com.enode.kafka;

import com.enode.queue.IMessageContext;
import com.enode.queue.QueueMessage;
import com.enode.queue.command.ConsumeStatus;
import com.enode.queue.domainevent.DomainEventListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class KafkaDomainEventConsumer extends DomainEventListener implements IMessageListener {

    @Override
    public ConsumeStatus receiveMessage(ConsumerRecord message, IMessageContext context) {
        QueueMessage queueMessage = KafkaTool.covertToQueueMessage(message);
        handle(queueMessage, context);
        return ConsumeStatus.CONSUME_SUCCESS;
    }

}
