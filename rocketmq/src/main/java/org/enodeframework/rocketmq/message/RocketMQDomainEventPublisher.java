package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.domainevent.AbstractDomainEventPublisher;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class RocketMQDomainEventPublisher extends AbstractDomainEventPublisher {
    private DefaultMQProducer producer;

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage eventStream) {
        QueueMessage queueMessage = createDomainEventStreamMessage(eventStream);
        Message message = RocketMQTool.covertToProducerRecord(queueMessage);
        return SendRocketMQService.sendMessageAsync(producer, message, queueMessage.getRouteKey());
    }

    public DefaultMQProducer getProducer() {
        return producer;
    }

    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }
}
