package com.enodeframework.queue.applicationmessage;

import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.infrastructure.IMessagePublisher;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.QueueMessageTypeCode;
import com.enodeframework.queue.TopicData;

public abstract class AbstractApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {
    protected TopicData topicData;

    public TopicData getTopicData() {
        return topicData;
    }

    public void setTopicData(TopicData topicData) {
        this.topicData = topicData;
    }

    protected QueueMessage createApplicationMessage(IApplicationMessage message) {
        String appMessageData = JsonTool.serialize(message);
        ApplicationDataMessage appDataMessage = new ApplicationDataMessage(appMessageData, message.getClass().getName());
        String data = JsonTool.serialize(appDataMessage);
        String routeKey = message.getRoutingKey() != null ? message.getRoutingKey() : message.id();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(data);
        queueMessage.setRouteKey(routeKey);
        queueMessage.setCode(QueueMessageTypeCode.ApplicationMessage.getValue());
        queueMessage.setKey(message.id());
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTags());
        return queueMessage;
    }
}
