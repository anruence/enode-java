package org.enodeframework.queue.applicationmessage;

import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.QueueMessage;

import java.util.concurrent.CompletableFuture;

public class DefaultApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {

    private final String topic;

    private final String tag;

    private final ISendMessageService producer;

    private final ISerializeService serializeService;

    public DefaultApplicationMessagePublisher(String topic, String tag, ISendMessageService producer, ISerializeService serializeService) {
        this.topic = topic;
        this.tag = tag;
        this.producer = producer;
        this.serializeService = serializeService;
    }

    protected QueueMessage createApplicationMessage(IApplicationMessage message) {
        Assert.nonNull(topic, "topic");
        String appMessageData = serializeService.serialize(message);
        ApplicationDataMessage appDataMessage = new ApplicationDataMessage(appMessageData, message.getClass().getName());
        String data = serializeService.serialize(appDataMessage);
        String routeKey = message.getId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(data);
        queueMessage.setRouteKey(routeKey);
        queueMessage.setKey(message.getId());
        queueMessage.setTopic(topic);
        queueMessage.setTag(tag);
        return queueMessage;
    }

    @Override
    public CompletableFuture<Boolean> publishAsync(IApplicationMessage message) {
        return producer.sendMessageAsync(createApplicationMessage(message));
    }
}
