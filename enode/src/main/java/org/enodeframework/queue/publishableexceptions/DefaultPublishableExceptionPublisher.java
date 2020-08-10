package org.enodeframework.queue.publishableexceptions;

import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.QueueMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DefaultPublishableExceptionPublisher implements IMessagePublisher<IDomainException> {

    private final String topic;

    private final String tag;

    private final ISendMessageService sendMessageService;

    private final ISerializeService serializeService;

    public DefaultPublishableExceptionPublisher(String topic, String tag, ISendMessageService sendMessageService, ISerializeService serializeService) {
        this.topic = topic;
        this.tag = tag;
        this.sendMessageService = sendMessageService;
        this.serializeService = serializeService;
    }

    protected QueueMessage createExceptionMessage(IDomainException exception) {
        Ensure.notNull(topic, "topic");
        Map<String, Object> serializableInfo = new HashMap<>();
        exception.serializeTo(serializableInfo);
        PublishableExceptionMessage exceptionMessage = new PublishableExceptionMessage();
        exceptionMessage.setUniqueId(exception.getId());
        exceptionMessage.setExceptionType(exception.getClass().getName());
        exceptionMessage.setTimestamp(exception.getTimestamp());
        exceptionMessage.setSerializableInfo(serializableInfo);
        exceptionMessage.setItems(exception.getItems());
        String data = serializeService.serialize(exceptionMessage);
        String routeKey = exception.getId();
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setTopic(topic);
        queueMessage.setTag(tag);
        queueMessage.setBody(data);
        queueMessage.setRouteKey(routeKey);
        queueMessage.setKey(exceptionMessage.getUniqueId());
        return queueMessage;
    }

    @Override
    public CompletableFuture<Void> publishAsync(IDomainException message) {
        return sendMessageService.sendMessageAsync(createExceptionMessage(message));
    }
}
