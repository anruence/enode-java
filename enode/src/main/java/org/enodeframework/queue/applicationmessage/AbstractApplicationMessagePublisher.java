package org.enodeframework.queue.applicationmessage;

import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.QueueMessageTypeCode;
import org.enodeframework.queue.TopicData;

public abstract class AbstractApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {

    private TopicData topicData;

    public TopicData getTopicData() {
        return topicData;
    }

    public void setTopicData(TopicData topicData) {
        this.topicData = topicData;
    }

    protected QueueMessage createApplicationMessage(IApplicationMessage message) {
        Ensure.notNull(topicData, "topicData");
        String appMessageData = JsonTool.serialize(message);
        ApplicationDataMessage appDataMessage = new ApplicationDataMessage(appMessageData, message.getClass().getName());
        String data = JsonTool.serialize(appDataMessage);
        String routeKey = message.getId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(data);
        queueMessage.setRouteKey(routeKey);
        queueMessage.setCode(QueueMessageTypeCode.ApplicationMessage.getValue());
        queueMessage.setKey(message.getId());
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTags());
        return queueMessage;
    }
}
