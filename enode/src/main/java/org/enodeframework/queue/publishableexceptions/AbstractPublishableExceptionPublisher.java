package org.enodeframework.queue.publishableexceptions;

import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.queue.QueueMessage;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPublishableExceptionPublisher implements IMessagePublisher<IDomainException> {
    private String topic;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    protected QueueMessage createExceptionMessage(IDomainException exception) {
        Ensure.notNull(topic, "topic");
        Map<String, String> serializableInfo = new HashMap<>();
        exception.serializeTo(serializableInfo);
        PublishableExceptionMessage exceptionMessage = new PublishableExceptionMessage();
        exceptionMessage.setUniqueId(exception.getId());
        exceptionMessage.setExceptionType(exception.getClass().getName());
        exceptionMessage.setTimestamp(exception.getTimestamp());
        exceptionMessage.setSerializableInfo(serializableInfo);
        exceptionMessage.setItems(exception.getItems());
        String data = JsonTool.serialize(exceptionMessage);
        String routeKey = exception.getId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setTopic(topic);
        queueMessage.setBody(data);
        queueMessage.setRouteKey(routeKey);
        queueMessage.setKey(exceptionMessage.getUniqueId());
        return queueMessage;
    }
}
