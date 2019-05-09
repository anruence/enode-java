package com.enode.rocketmq.message;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.enode.common.io.AsyncTaskResult;
import com.enode.infrastructure.IPublishableException;
import com.enode.queue.QueueMessage;
import com.enode.queue.publishableexceptions.PublishableExceptionPublisher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class RocketMQPublishableExceptionPublisher extends PublishableExceptionPublisher {

    public DefaultMQProducer getProducer() {
        return producer;
    }

    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

    private DefaultMQProducer producer;

    @Autowired
    protected SendRocketMQService _sendMessageService;

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException exception) {
        QueueMessage queueMessage = createExecptionMessage(exception);
        Message message = RocketMQTool.covertToProducerRecord(queueMessage);
        return _sendMessageService.sendMessageAsync(producer, message, queueMessage.getRouteKey());
    }
}
