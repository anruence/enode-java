package com.enode.rocketmq.publishableexceptions;

import com.alibaba.rocketmq.common.message.Message;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.utilities.BitConverter;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.infrastructure.IPublishableException;
import com.enode.infrastructure.ISequenceMessage;
import com.enode.rocketmq.QueueMessageTypeCode;
import com.enode.rocketmq.ITopicProvider;
import com.enode.rocketmq.SendRocketMQService;
import com.enode.rocketmq.TopicTagData;
import com.enode.rocketmq.client.Producer;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PublishableExceptionPublisher implements IMessagePublisher<IPublishableException> {
    private final IJsonSerializer _jsonSerializer;
    private final ITopicProvider<IPublishableException> _exceptionTopicProvider;
    private final Producer _producer;
    private final SendRocketMQService _sendMessageService;

    @Inject
    public PublishableExceptionPublisher(Producer producer, IJsonSerializer jsonSerializer,
                                         ITopicProvider<IPublishableException> exceptionITopicProvider,
                                         SendRocketMQService sendQueueMessageService) {
        _producer = producer;
        _jsonSerializer = jsonSerializer;
        _exceptionTopicProvider = exceptionITopicProvider;
        _sendMessageService = sendQueueMessageService;
    }

    public Producer getProducer() {
        return _producer;
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
                QueueMessageTypeCode.ExceptionMessage.getValue(), // flag
                BitConverter.getBytes(data), // body
                true);
    }
}
