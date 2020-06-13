package org.enodeframework.queue.applicationmessage;

import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.queue.QueueMessage;

public abstract class AbstractApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {

    private String topic;

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
        return queueMessage;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
