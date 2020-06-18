package org.enodeframework.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author anruence@gmail.com
 */
public class KafkaPublishableExceptionListener implements AcknowledgingMessageListener<String, String> {

    private final IMessageHandler publishableExceptionListener;

    public KafkaPublishableExceptionListener(IMessageHandler publishableExceptionListener) {
        this.publishableExceptionListener = publishableExceptionListener;
    }

    /**
     * Invoked with data from kafka.
     *
     * @param data           the data to be processed.
     * @param acknowledgment the acknowledgment.
     */
    @Override
    public void onMessage(ConsumerRecord data, Acknowledgment acknowledgment) {
        QueueMessage queueMessage = KafkaTool.covertToQueueMessage(data);
        publishableExceptionListener.handle(queueMessage, context -> {
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        });
    }
}
