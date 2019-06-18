package com.enodeframework.kafka;

import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.domainevent.AbstractDomainEventListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

/**
 * @author anruence@gmail.com
 */
public class KafkaDomainEventListener extends AbstractDomainEventListener implements AcknowledgingMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(KafkaDomainEventListener.class);

    /**
     * Invoked with data from kafka. The default implementation throws
     * {@link UnsupportedOperationException}.
     *
     * @param data           the data to be processed.
     * @param acknowledgment the acknowledgment.
     */
    @Override
    public void onMessage(ConsumerRecord data, Acknowledgment acknowledgment) {
        QueueMessage queueMessage = KafkaTool.covertToQueueMessage(data);
        handle(queueMessage, context -> {
        });
        if (acknowledgment != null) {
            acknowledgment.acknowledge();
        }
    }

    /**
     * Invoked with data from kafka.
     *
     * @param data the data to be processed.
     */
    @Override
    public void onMessage(Object data) {
        logger.info("receive data:{}", data);
    }
}
