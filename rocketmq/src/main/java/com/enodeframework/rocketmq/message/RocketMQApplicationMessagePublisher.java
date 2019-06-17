package com.enodeframework.rocketmq.message;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.applicationmessage.AbstractApplicationMessagePublisher;

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
