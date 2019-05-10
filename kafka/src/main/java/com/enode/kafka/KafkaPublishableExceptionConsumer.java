package com.enode.kafka;

import com.enode.queue.IMessageContext;
import com.enode.queue.QueueMessage;
import com.enode.queue.command.ConsumeStatus;
import com.enode.queue.publishableexceptions.PublishableExceptionConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class KafkaPublishableExceptionConsumer extends PublishableExceptionConsumer implements IMessageListener {

    @Override
    public ConsumeStatus receiveMessage(ConsumerRecord message, IMessageContext context) {
        QueueMessage queueMessage = KafkaTool.covertToQueueMessage(message);
        handle(queueMessage, context);
        return ConsumeStatus.CONSUME_SUCCESS;
    }

}
