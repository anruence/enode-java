package com.qianzhui.enode.rocketmq.publishableexceptions;

import com.alibaba.rocketmq.common.message.Message;
import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.common.serializing.IJsonSerializer;
import com.qianzhui.enode.common.utilities.BitConverter;
import com.qianzhui.enode.infrastructure.IMessagePublisher;
import com.qianzhui.enode.infrastructure.IPublishableException;
import com.qianzhui.enode.infrastructure.ISequenceMessage;
import com.qianzhui.enode.rocketmq.ITopicProvider;
import com.qianzhui.enode.rocketmq.RocketMQMessageTypeCode;
import com.qianzhui.enode.rocketmq.SendQueueMessageService;
import com.qianzhui.enode.rocketmq.TopicTagData;
import com.qianzhui.enode.rocketmq.client.Producer;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PublishableExceptionPublisher implements IMessagePublisher<IPublishableException> {
    private final IJsonSerializer _jsonSerializer;
    private final ITopicProvider<IPublishableException> _exceptionTopicProvider;
    private final Producer _producer;
    private final SendQueueMessageService _sendMessageService;

    public Producer getProducer() {
        return _producer;
    }

    @Inject
    public PublishableExceptionPublisher(Producer producer, IJsonSerializer jsonSerializer,
                                         ITopicProvider<IPublishableException> exceptionITopicProvider,
                                         SendQueueMessageService sendQueueMessageService) {
        _producer = producer;
        _jsonSerializer = jsonSerializer;
        _exceptionTopicProvider = exceptionITopicProvider;
        _sendMessageService = sendQueueMessageService;
    }

    public PublishableExceptionPublisher start() {
        return this;
    }

    public PublishableExceptionPublisher shutdown() {
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IPublishableException exception) {
        Message message = createEQueueMessage(exception);
        return _sendMessageService.sendMessageAsync(_producer, message, exception.getRoutingKey() == null ? exception.id() : exception.getRoutingKey(), exception.id(), null);
    }

    private Message createEQueueMessage(IPublishableException exception) {
        TopicTagData topicTagData = _exceptionTopicProvider.getPublishTopic(exception);
        Map<String, String> serializableInfo = new HashMap<>();
        exception.serializeTo(serializableInfo);
        ISequenceMessage sequenceMessage = null;
        if (exception instanceof ISequenceMessage) {
            sequenceMessage = (ISequenceMessage) exception;
        }

        PublishableExceptionMessage publishableExceptionMessage = new PublishableExceptionMessage();
        publishableExceptionMessage.setUniqueId(exception.id());
        publishableExceptionMessage.setAggregateRootTypeName(sequenceMessage != null ? sequenceMessage.aggregateRootTypeName() : null);
        publishableExceptionMessage.setAggregateRootId(sequenceMessage != null ? sequenceMessage.aggregateRootStringId() : null);
        publishableExceptionMessage.setExceptionType(exception.getClass().getName());
        publishableExceptionMessage.setTimestamp(exception.timestamp());
        publishableExceptionMessage.setSerializableInfo(serializableInfo);

        String data = _jsonSerializer.serialize(publishableExceptionMessage);

        return new Message(topicTagData.getTopic(), //topic
//                _typeNameProvider.getTypeName(exception.getClass()), //tags
                topicTagData.getTag(), //tag
                exception.id(), // keys
                RocketMQMessageTypeCode.ExceptionMessage.getValue(), // flag
                BitConverter.getBytes(data), // body
                true);
    }
}
