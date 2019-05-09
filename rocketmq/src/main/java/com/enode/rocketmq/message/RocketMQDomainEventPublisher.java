package com.enode.rocketmq.message;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.common.message.Message;
import com.enode.common.io.AsyncTaskResult;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.queue.QueueMessage;
import com.enode.queue.domainevent.DomainEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class RocketMQDomainEventPublisher extends DomainEventPublisher {

    @Autowired
    private SendRocketMQService _sendMessageService;

    private DefaultMQProducer producer;

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage eventStream) {
        QueueMessage queueMessage = createDomainEventStreamMessage(eventStream);
        Message message = RocketMQTool.covertToProducerRecord(queueMessage);
        return _sendMessageService.sendMessageAsync(producer, message, queueMessage.getRouteKey());
    }

    public DefaultMQProducer getProducer() {
        return producer;
    }

    public void setProducer(DefaultMQProducer producer) {
        this.producer = producer;
    }

}
