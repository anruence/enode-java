package com.enode.rocketmq.message;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.queue.QueueMessage;
import com.enode.queue.applicationmessage.ApplicationMessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class RocketMQApplicationMessagePublisher extends ApplicationMessagePublisher {

    @Autowired
    private SendRocketMQService _sendMessageService;

    public DefaultMQProducer getProducer() {
        return producer;
    }

    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

    private DefaultMQProducer producer;

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        QueueMessage queueMessage = createApplicationMessage(message);
        Message msg = RocketMQTool.covertToProducerRecord(queueMessage);
        return _sendMessageService.sendMessageAsync(producer, msg, queueMessage.getRouteKey());
    }
}
