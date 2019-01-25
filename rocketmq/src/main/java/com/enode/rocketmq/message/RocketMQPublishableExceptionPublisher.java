package com.enode.rocketmq.message;

import com.alibaba.rocketmq.common.message.Message;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.infrastructure.IPublishableException;
import com.enode.queue.ITopicProvider;
import com.enode.queue.QueueMessage;
import com.enode.queue.publishableexceptions.PublishableExceptionPublisher;
import com.enode.rocketmq.client.Producer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
public class RocketMQPublishableExceptionPublisher extends PublishableExceptionPublisher {

    private Producer _producer;

    protected SendRocketMQService _sendMessageService;

    @Inject
    public RocketMQPublishableExceptionPublisher(Producer producer, IJsonSerializer jsonSerializer, ITopicProvider<IPublishableException> exceptionTopicProvider, SendRocketMQService sendMessageService) {
        _jsonSerializer = jsonSerializer;
        _exceptionTopicProvider = exceptionTopicProvider;
        _sendMessageService = sendMessageService;
        _producer = producer;
    }

    @Override
    public RocketMQPublishableExceptionPublisher start() {
        super.start();
        _producer.start();
        return this;
    }

    @Override
    public RocketMQPublishableExceptionPublisher shutdown() {
        _producer.shutdown();
        super.shutdown();
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException exception) {
        QueueMessage queueMessage = createExecptionMessage(exception);
        Message message = RocketMQTool.covertToProducerRecord(queueMessage);
        return _sendMessageService.sendMessageAsync(_producer, message, queueMessage.getRouteKey());
    }
}
