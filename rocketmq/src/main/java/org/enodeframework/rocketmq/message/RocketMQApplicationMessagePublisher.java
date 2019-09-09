package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.enodeframework.applicationmessage.IApplicationMessage;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.applicationmessage.AbstractApplicationMessagePublisher;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class RocketMQApplicationMessagePublisher extends AbstractApplicationMessagePublisher {
    private DefaultMQProducer producer;

    public DefaultMQProducer getProducer() {
        return producer;
    }

    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        QueueMessage queueMessage = createApplicationMessage(message);
        Message msg = RocketMQTool.covertToProducerRecord(queueMessage);
        return SendRocketMQService.sendMessageAsync(producer, msg, queueMessage.getRouteKey());
    }
}
