package org.enodeframework.queue.domainevent;

import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.QueueMessage;

import java.util.concurrent.CompletableFuture;

public class DefaultDomainEventPublisher implements IMessagePublisher<DomainEventStreamMessage> {

    private final IEventSerializer eventSerializer;

    private final ISendMessageService producer;

    private final String topic;

    public DefaultDomainEventPublisher(String topic, IEventSerializer eventSerializer, ISendMessageService producer) {
        this.eventSerializer = eventSerializer;
        this.producer = producer;
        this.topic = topic;
    }

    protected QueueMessage createDomainEventStreamMessage(DomainEventStreamMessage eventStream) {
        Ensure.notNull(eventStream.getAggregateRootId(), "aggregateRootId");
        Ensure.notNull(topic, "topic");
        EventStreamMessage message = new EventStreamMessage();
        message.setId(eventStream.getId());
        message.setCommandId(eventStream.getCommandId());
        message.setAggregateRootTypeName(eventStream.getAggregateRootTypeName());
        message.setAggregateRootId(eventStream.getAggregateRootId());
        message.setTimestamp(eventStream.getTimestamp());
        message.setVersion(eventStream.getVersion());
        message.setEvents(eventSerializer.serialize(eventStream.getEvents()));
        message.setItems(eventStream.getItems());
        String data = JsonTool.serialize(message);
        String routeKey = message.getAggregateRootId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setTopic(topic);
        queueMessage.setBody(data);
        queueMessage.setRouteKey(routeKey);
        queueMessage.setKey(message.getId());
        return queueMessage;
    }

    @Override
    public CompletableFuture<Void> publishAsync(DomainEventStreamMessage message) {
        return producer.sendMessageAsync(createDomainEventStreamMessage(message));
    }
}
