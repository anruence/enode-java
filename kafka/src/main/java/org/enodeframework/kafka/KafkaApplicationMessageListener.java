package org.enodeframework.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.applicationmessage.AbstractApplicationMessageListener;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author anruence@gmail.com
 */
public class KafkaApplicationMessageListener extends AbstractApplicationMessageListener implements AcknowledgingMessageListener {
    /**
     * Invoked with data from kafka.
     *
     * @param data           the data to be processed.
     * @param acknowledgment the acknowledgment.
     */
    @Override
    public void onMessage(ConsumerRecord data, Acknowledgment acknowledgment) {
        QueueMessage queueMessage = KafkaTool.covertToQueueMessage(data);
        handle(queueMessage, context -> {
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        });
    }

    /**
     * Invoked with data from kafka.
     *
     * @param data the data to be processed.
     */
    @Override
    public void onMessage(Object data) {
    }
}
