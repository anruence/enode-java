package com.enode.rocketmq.applicationmessage;

import com.alibaba.rocketmq.common.message.Message;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.utilities.BitConverter;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.infrastructure.ITypeNameProvider;
import com.enode.message.ApplicationDataMessage;
import com.enode.rocketmq.ITopicProvider;
import com.enode.message.MessageTypeCode;
import com.enode.rocketmq.SendRocketMQService;
import com.enode.rocketmq.TopicTagData;
import com.enode.rocketmq.client.Producer;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class ApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {

    private final IJsonSerializer _jsonSerializer;
    private final ITopicProvider<IApplicationMessage> _messageTopicProvider;
    private final ITypeNameProvider _typeNameProvider;
    private final Producer _producer;
    private final SendRocketMQService _sendMessageService;

    public Producer getProducer() {
        return _producer;
    }

    @Inject
    public ApplicationMessagePublisher(Producer producer, IJsonSerializer jsonSerializer,
                                       ITopicProvider<IApplicationMessage> messageITopicProvider,
                                       ITypeNameProvider typeNameProvider,
                                       SendRocketMQService sendQueueMessageService) {
        _producer = producer;
        _jsonSerializer = jsonSerializer;
        _messageTopicProvider = messageITopicProvider;
        _typeNameProvider = typeNameProvider;
        _sendMessageService = sendQueueMessageService;
    }

    public ApplicationMessagePublisher start() {
        return this;
    }

    public ApplicationMessagePublisher shutdown() {
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        Message queueMessage = createEQueueMessage(message);
        return _sendMessageService.sendMessageAsync(_producer, queueMessage, message.getRoutingKey() == null ? message.id() : message.getRoutingKey(), message.id(), null);
    }

    private Message createEQueueMessage(IApplicationMessage message) {
        TopicTagData topicTagData = _messageTopicProvider.getPublishTopic(message);
        String appMessageData = _jsonSerializer.serialize(message);
        ApplicationDataMessage appDataMessage = new ApplicationDataMessage(appMessageData, message.getClass().getName());

        String data = _jsonSerializer.serialize(appDataMessage);

        Message mqMessage =  new Message(topicTagData.getTopic(), //topic
                //_typeNameProvider.getTypeName(message.getClass()), //tags
                topicTagData.getTag(), //tag
                message.id(), // keys
                MessageTypeCode.ApplicationMessage.getValue(), // flag
                BitConverter.getBytes(data), // body
                true);


        return mqMessage;
    }
}
