package com.enodeframework.queue.domainevent;

import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.eventing.IEventSerializer;
import com.enodeframework.infrastructure.IMessagePublisher;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.QueueMessageTypeCode;
import com.enodeframework.queue.TopicData;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDomainEventPublisher implements IMessagePublisher<DomainEventStreamMessage> {
    @Autowired
    protected IEventSerializer eventSerializer;

    private TopicData topicData;

    public TopicData getTopicData() {
        return topicData;
    }

    public void setTopicData(TopicData topicData) {
        this.topicData = topicData;
    }

    protected QueueMessage createDomainEventStreamMessage(DomainEventStreamMessage eventStream) {
        Ensure.notNull(eventStream.aggregateRootId(), "aggregateRootId");
        EventStreamMessage eventMessage = createEventMessage(eventStream);
        String data = JsonTool.serialize(eventMessage);
        String routeKey = eventStream.getRoutingKey() != null ? eventStream.getRoutingKey() : eventMessage.getAggregateRootId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setCode(QueueMessageTypeCode.DomainEventStreamMessage.getValue());
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTags());
        queueMessage.setBody(data);
        queueMessage.setKey(eventStream.id());
        queueMessage.setRouteKey(routeKey);
        queueMessage.setVersion(eventStream.version());
        return queueMessage;
    }

    private EventStreamMessage createEventMessage(DomainEventStreamMessage eventStream) {
        EventStreamMessage message = new EventStreamMessage();
        message.setId(eventStream.id());
        message.setCommandId(eventStream.getCommandId());
        message.setAggregateRootTypeName(eventStream.aggregateRootTypeName());
        message.setAggregateRootId(eventStream.aggregateRootId());
        message.setTimestamp(eventStream.timestamp());
        message.setVersion(eventStream.version());
        message.setEvents(eventSerializer.serialize(eventStream.getEvents()));
        message.setItems(eventStream.getItems());
        return message;
    }
}
