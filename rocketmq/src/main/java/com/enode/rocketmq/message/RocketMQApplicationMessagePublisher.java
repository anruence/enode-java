package com.enode.rocketmq.message;

import com.alibaba.rocketmq.common.message.Message;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.queue.ITopicProvider;
import com.enode.queue.QueueMessage;
import com.enode.queue.applicationmessage.ApplicationMessagePublisher;
import com.enode.rocketmq.client.Producer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
public class RocketMQApplicationMessagePublisher extends ApplicationMessagePublisher {

    private SendRocketMQService _sendMessageService;

    private Producer _producer;

    @Inject
    public RocketMQApplicationMessagePublisher(Producer producer, IJsonSerializer jsonSerializer, ITopicProvider<IApplicationMessage> messageTopicProvider, SendRocketMQService sendMessageService) {
        _jsonSerializer = jsonSerializer;
        _messageTopicProvider = messageTopicProvider;
        _sendMessageService = sendMessageService;
        _producer = producer;
    }

    @Override
    public RocketMQApplicationMessagePublisher start() {
        super.start();
        _producer.start();
        return this;
    }

    @Override
    public RocketMQApplicationMessagePublisher shutdown() {
        _producer.shutdown();
        super.shutdown();
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        QueueMessage queueMessage = createApplicationMessage(message);
        Message msg = RocketMQTool.covertToProducerRecord(queueMessage);
        return _sendMessageService.sendMessageAsync(_producer, msg, queueMessage.getRouteKey());
    }
}
