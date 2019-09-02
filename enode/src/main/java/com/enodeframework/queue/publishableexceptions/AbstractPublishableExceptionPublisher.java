package com.enodeframework.queue.publishableexceptions;

import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.messaging.IMessagePublisher;
import com.enodeframework.publishableexception.IPublishableException;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.QueueMessageTypeCode;
import com.enodeframework.queue.TopicData;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPublishableExceptionPublisher implements IMessagePublisher<IPublishableException> {
    private TopicData topicData;

    public TopicData getTopicData() {
        return topicData;
    }

    public void setTopicData(TopicData topicData) {
        this.topicData = topicData;
    }

    protected QueueMessage createExceptionMessage(IPublishableException exception) {
        Ensure.notNull(topicData, "topicData");
        Map<String, String> serializableInfo = new HashMap<>();
        exception.serializeTo(serializableInfo);
        PublishableExceptionMessage exceptionMessage = new PublishableExceptionMessage();
        exceptionMessage.setUniqueId(exception.getId());
        exceptionMessage.setExceptionType(exception.getClass().getName());
        exceptionMessage.setTimestamp(exception.getTimestamp());
        exceptionMessage.setSerializableInfo(serializableInfo);
        String data = JsonTool.serialize(exceptionMessage);
        String routeKey = exception.getId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setCode(QueueMessageTypeCode.ExceptionMessage.getValue());
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTags());
        queueMessage.setBody(data);
        queueMessage.setKey(exception.getId());
        queueMessage.setRouteKey(routeKey);
        return queueMessage;
    }
}
