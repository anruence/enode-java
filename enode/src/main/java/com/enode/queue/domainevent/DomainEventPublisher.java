package com.enode.queue.domainevent;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.utilities.Ensure;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.eventing.IEventSerializer;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.queue.QueueMessage;
import com.enode.queue.QueueMessageTypeCode;
import com.enode.queue.TopicData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public abstract class DomainEventPublisher implements IMessagePublisher<DomainEventStreamMessage> {

    @Autowired
    protected IJsonSerializer _jsonSerializer;

    @Autowired
    protected IEventSerializer _eventSerializer;
    protected TopicData topicData;

    public TopicData getTopicData() {
        return topicData;
    }

    public void setTopicData(TopicData topicData) {
        this.topicData = topicData;
    }

    public DomainEventPublisher start() {
        return this;
    }

    public DomainEventPublisher shutdown() {
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage eventStream) {
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    protected QueueMessage createDomainEventStreamMessage(DomainEventStreamMessage eventStream) {
        Ensure.notNull(eventStream.aggregateRootId(), "aggregateRootId");
        EventStreamMessage eventMessage = createEventMessage(eventStream);
        IDomainEvent domainEvent = eventStream.getEvents().size() > 0 ? eventStream.getEvents().get(0) : null;
        String data = _jsonSerializer.serialize(eventMessage);
        String routeKey = eventStream.getRoutingKey() != null ? eventStream.getRoutingKey() : eventMessage.getAggregateRootId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setCode(QueueMessageTypeCode.DomainEventStreamMessage.getValue());
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTag());
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
        message.setEvents(_eventSerializer.serialize(eventStream.getEvents()));
        message.setItems(eventStream.getItems());
        return message;
    }
}
