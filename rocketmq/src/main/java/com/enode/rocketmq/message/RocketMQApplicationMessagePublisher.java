package com.enode.rocketmq.message;

import com.alibaba.rocketmq.common.message.Message;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.queue.ITopicProvider;
import com.enode.queue.QueueMessage;
import com.enode.queue.applicationmessage.ApplicationMessagePublisher;
import com.enode.rocketmq.client.Producer;
import com.enode.rocketmq.client.RocketMQFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Singleton
public class RocketMQApplicationMessagePublisher extends ApplicationMessagePublisher {

    private SendRocketMQService _sendMessageService;

    private Producer _producer;

    private RocketMQFactory _mqFactory;

    @Inject
    public RocketMQApplicationMessagePublisher(RocketMQFactory mqFactory, IJsonSerializer jsonSerializer, ITopicProvider<IApplicationMessage> messageTopicProvider, SendRocketMQService sendMessageService) {
        _jsonSerializer = jsonSerializer;
        _messageTopicProvider = messageTopicProvider;
        _sendMessageService = sendMessageService;
        _mqFactory = mqFactory;
    }

    public RocketMQApplicationMessagePublisher initializeQueue(Properties properties) {
        _producer = _mqFactory.createProducer(properties);
        return this;
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
