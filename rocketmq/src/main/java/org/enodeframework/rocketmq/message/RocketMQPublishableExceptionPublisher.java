package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.publishableexceptions.AbstractPublishableExceptionPublisher;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class RocketMQPublishableExceptionPublisher extends AbstractPublishableExceptionPublisher {
    private DefaultMQProducer producer;

    public DefaultMQProducer getProducer() {
        return producer;
    }

    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<Void> publishAsync(IDomainException exception) {
        QueueMessage queueMessage = createExceptionMessage(exception);
        Message message = RocketMQTool.covertToProducerRecord(queueMessage);
        return SendRocketMQService.sendMessageAsync(producer, message, queueMessage.getRouteKey());
    }
}
