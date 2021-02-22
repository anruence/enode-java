package org.enodeframework.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author anruence@gmail.com
 */
public class KafkaMessageListener implements AcknowledgingMessageListener<String, String> {

    private final IMessageHandler messageHandler;

    public KafkaMessageListener(IMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * Invoked with data from kafka.
     *
     * @param data           the data to be processed.
     * @param acknowledgment the acknowledgment.
     */
    @Override
    public void onMessage(ConsumerRecord<String, String> data, Acknowledgment acknowledgment) {
        QueueMessage queueMessage = KafkaTool.covertToQueueMessage(data);
        messageHandler.handle(queueMessage, context -> {
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        });
    }
}
