package com.enodeframework.queue.publishableexceptions;

import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.infrastructure.IMessagePublisher;
import com.enodeframework.infrastructure.IPublishableException;
import com.enodeframework.infrastructure.ISequenceMessage;
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
        ISequenceMessage sequenceMessage = null;
        if (exception instanceof ISequenceMessage) {
            sequenceMessage = (ISequenceMessage) exception;
        }
        PublishableExceptionMessage exceptionMessage = new PublishableExceptionMessage();
        exceptionMessage.setUniqueId(exception.id());
        exceptionMessage.setAggregateRootTypeName(sequenceMessage != null ? sequenceMessage.aggregateRootTypeName() : null);
        exceptionMessage.setAggregateRootId(sequenceMessage != null ? sequenceMessage.aggregateRootStringId() : null);
        exceptionMessage.setExceptionType(exception.getClass().getName());
        exceptionMessage.setTimestamp(exception.timestamp());
        exceptionMessage.setSerializableInfo(serializableInfo);
        String data = JsonTool.serialize(exceptionMessage);
        String routeKey = exception.getRoutingKey() == null ? exception.getRoutingKey() : exception.id();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setCode(QueueMessageTypeCode.ExceptionMessage.getValue());
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTags());
        queueMessage.setBody(data);
        queueMessage.setKey(exception.id());
        queueMessage.setRouteKey(routeKey);
        return queueMessage;
    }

}
