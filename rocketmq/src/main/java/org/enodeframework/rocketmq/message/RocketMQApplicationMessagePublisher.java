package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.common.message.Message;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.applicationmessage.AbstractApplicationMessagePublisher;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class RocketMQApplicationMessagePublisher extends AbstractApplicationMessagePublisher {

    private MQProducer producer;

    @Override
    public CompletableFuture<Void> publishAsync(IApplicationMessage message) {
        QueueMessage queueMessage = createApplicationMessage(message);
        Message msg = RocketMQTool.covertToProducerRecord(queueMessage);
        return SendRocketMQService.sendMessageAsync(producer, msg, queueMessage.getRouteKey());
    }

    public MQProducer getProducer() {
        return producer;
    }

    public void setProducer(MQProducer producer) {
        this.producer = producer;
    }
}
