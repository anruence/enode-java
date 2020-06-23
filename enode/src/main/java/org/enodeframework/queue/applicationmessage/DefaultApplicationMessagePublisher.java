package org.enodeframework.queue.applicationmessage;

import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.QueueMessage;

import java.util.concurrent.CompletableFuture;

public class DefaultApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {

    private final String topic;

    private final String tag;

    private final ISendMessageService producer;

    public DefaultApplicationMessagePublisher(String topic, String tag, ISendMessageService producer) {
        this.topic = topic;
        this.tag = tag;
        this.producer = producer;
    }

    protected QueueMessage createApplicationMessage(IApplicationMessage message) {
        Ensure.notNull(topic, "topic");
        String appMessageData = JsonTool.serialize(message);
        ApplicationDataMessage appDataMessage = new ApplicationDataMessage(appMessageData, message.getClass().getName());
        String data = JsonTool.serialize(appDataMessage);
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
    public CompletableFuture<Void> publishAsync(IApplicationMessage message) {
        return producer.sendMessageAsync(createApplicationMessage(message));
    }
}
