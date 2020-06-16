package org.enodeframework.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author anruence@gmail.com
 */
public class KafkaDomainEventListener implements AcknowledgingMessageListener<String, String> {

    private final IMessageHandler domainEventListener;

    public KafkaDomainEventListener(IMessageHandler domainEventListener) {
        this.domainEventListener = domainEventListener;
    }

    /**
     * Invoked with data from kafka. The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param data           the data to be processed.
     * @param acknowledgment the acknowledgment.
     */
    @Override
    public void onMessage(ConsumerRecord<String, String> data, Acknowledgment acknowledgment) {
        QueueMessage queueMessage = KafkaTool.covertToQueueMessage(data);
        domainEventListener.handle(queueMessage, context -> {
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }
        });
    }
}
