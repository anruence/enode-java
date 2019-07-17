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

    protected IEventSerializer eventSerializer;
    private TopicData topicData;

    @Autowired
    public void setEventSerializer(IEventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
    }

    public TopicData getTopicData() {
        return topicData;
    }

    public void setTopicData(TopicData topicData) {
        this.topicData = topicData;
    }

    protected QueueMessage createDomainEventStreamMessage(DomainEventStreamMessage eventStream) {
        Ensure.notNull(eventStream.getAggregateRootId(), "aggregateRootId");
        Ensure.notNull(topicData, "topicData");
        EventStreamMessage eventMessage = createEventMessage(eventStream);
        String data = JsonTool.serialize(eventMessage);
        String routeKey = eventStream.getRoutingKey() != null ? eventStream.getRoutingKey() : eventMessage.getAggregateRootId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setCode(QueueMessageTypeCode.DomainEventStreamMessage.getValue());
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTags());
        queueMessage.setBody(data);
        queueMessage.setKey(eventStream.getId());
        queueMessage.setRouteKey(routeKey);
        queueMessage.setVersion(eventStream.getVersion());
        return queueMessage;
    }

    private EventStreamMessage createEventMessage(DomainEventStreamMessage eventStream) {
        EventStreamMessage message = new EventStreamMessage();
        message.setId(eventStream.getId());
        message.setCommandId(eventStream.getCommandId());
        message.setAggregateRootTypeName(eventStream.getAggregateRootTypeName());
        message.setAggregateRootId(eventStream.getAggregateRootId());
        message.setTimestamp(eventStream.getTimestamp());
        message.setVersion(eventStream.getVersion());
        message.setEvents(eventSerializer.serialize(eventStream.getEvents()));
        message.setItems(eventStream.getItems());
        return message;
    }
}
