package com.enode.kafka;

import com.enode.commanding.ICommand;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.common.remoting.common.RemotingUtil;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.utilities.Ensure;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.eventing.IEventSerializer;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IPublishableException;
import com.enode.infrastructure.ISequenceMessage;
import com.enode.queue.IMQProducer;
import com.enode.queue.ITopicProvider;
import com.enode.queue.TopicData;
import com.enode.queue.applicationmessage.ApplicationDataMessage;
import com.enode.queue.command.CommandMessage;
import com.enode.queue.command.CommandResultProcessor;
import com.enode.queue.domainevent.EventStreamMessage;
import com.enode.queue.publishableexceptions.PublishableExceptionMessage;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SendKafkaService implements IMQProducer {

    private IJsonSerializer _jsonSerializer;
    private ITopicProvider<ICommand> _commandTopicProvider;
    private ITopicProvider<IApplicationMessage> _messageTopicProvider;
    private ITopicProvider<IPublishableException> _exceptionTopicProvider;
    private ITopicProvider<IDomainEvent> _eventTopicProvider;
    private IEventSerializer _eventSerializer;
    private CommandResultProcessor _commandResultProcessor;
    private KafkaProducer _producer;

    @Inject
    public SendKafkaService(
            IJsonSerializer jsonSerializer,
            ITopicProvider<ICommand> commandTopicProvider,
            ITopicProvider<IApplicationMessage> messageTopicProvider,
            ITopicProvider<IPublishableException> exceptionTopicProvider,
            ITopicProvider<IDomainEvent> eventTopicProvider,
            IEventSerializer eventSerializer,
            CommandResultProcessor commandResultProcessor,
            KafkaProducer producer
    ) {
        _jsonSerializer = jsonSerializer;
        _commandTopicProvider = commandTopicProvider;
        _messageTopicProvider = messageTopicProvider;
        _exceptionTopicProvider = exceptionTopicProvider;
        _eventTopicProvider = eventTopicProvider;
        _eventSerializer = eventSerializer;
        _commandResultProcessor = commandResultProcessor;
        _producer = producer;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(IMessage msg, String routingKey) {
        return sendAsync(msg, routingKey, false);
    }

    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(IMessage msg, String routingKey, boolean sendReply) {
        CompletableFuture<AsyncTaskResult> promise = new CompletableFuture<>();

        ProducerRecord<String, String> message = null;
        if (msg instanceof ICommand) {
            ICommand commandMessage = (ICommand) msg;
            message = buildCommandMessage(commandMessage, routingKey, sendReply);

        }
        if (msg instanceof IApplicationMessage) {
            IApplicationMessage applicationMessage = (IApplicationMessage) msg;
            message = createApplicationMessage(applicationMessage, routingKey);
        }
        if (msg instanceof PublishableExceptionMessage) {
            IPublishableException exceptionMessage = (IPublishableException) msg;
            message = createExecptionMessage(exceptionMessage, routingKey);
        }
        if (msg instanceof DomainEventStreamMessage) {
            DomainEventStreamMessage eventStreamMessage = (DomainEventStreamMessage) msg;
            message = createDomainEventStreamMessage(eventStreamMessage, routingKey);
        }

        _producer.send(message, (metadata, e) -> {
            if (e != null) {
                promise.complete(new AsyncTaskResult(AsyncTaskStatus.IOException, e.getMessage()));
                return;
            }
            promise.complete(AsyncTaskResult.Success);
        });
        return promise;
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
        _producer.close();
    }

    private Integer getPartionByRouteKey(Object routingKey, String topic) {
        int hash = Math.abs(routingKey.hashCode());
        return hash % _producer.partitionsFor(topic).size();
    }

    private ProducerRecord<String, String> createDomainEventStreamMessage(DomainEventStreamMessage eventStream, String routeKey) {
        Ensure.notNull(eventStream.aggregateRootId(), "aggregateRootId");
        EventStreamMessage eventMessage = createEventMessage(eventStream);
        IDomainEvent domainEvent = eventStream.getEvents().size() > 0 ? eventStream.getEvents().get(0) : null;
        TopicData topicTagData = _eventTopicProvider.getPublishTopic(domainEvent);
        String data = _jsonSerializer.serialize(eventMessage);
        //事件流唯一id，聚合根id，命令id
        String key = String.format("%s %s %s",
                eventStream.id(),
                "event_agg_" + eventStream.aggregateRootStringId(),
                "event_cmd_" + eventStream.getCommandId()
        );
        return new ProducerRecord<>(
                topicTagData.getTopic(),
                getPartionByRouteKey(routeKey, topicTagData.getTopic()),
                key,
                data);
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

    private ProducerRecord<String, String> createApplicationMessage(IApplicationMessage message, String routeKey) {
        TopicData topicTagData = _messageTopicProvider.getPublishTopic(message);
        String appMessageData = _jsonSerializer.serialize(message);
        ApplicationDataMessage appDataMessage = new ApplicationDataMessage(appMessageData, message.getClass().getName());
        String data = _jsonSerializer.serialize(appDataMessage);
        return new ProducerRecord<>(
                topicTagData.getTopic(),
                getPartionByRouteKey(routeKey, topicTagData.getTopic()),
                message.id(), data);
    }

    private ProducerRecord<String, String> createExecptionMessage(IPublishableException exception, String routeKey) {
        TopicData topicTagData = _exceptionTopicProvider.getPublishTopic(exception);
        Map<String, String> serializableInfo = new HashMap<>();
        exception.serializeTo(serializableInfo);
        ISequenceMessage sequenceMessage = null;
        if (exception instanceof ISequenceMessage) {
            sequenceMessage = (ISequenceMessage) exception;
        }

        PublishableExceptionMessage publishableExceptionMessage = new PublishableExceptionMessage();
        publishableExceptionMessage.setUniqueId(exception.id());
        publishableExceptionMessage.setAggregateRootTypeName(sequenceMessage != null ? sequenceMessage.aggregateRootTypeName() : null);
        publishableExceptionMessage.setAggregateRootId(sequenceMessage != null ? sequenceMessage.aggregateRootStringId() : null);
        publishableExceptionMessage.setExceptionType(exception.getClass().getName());
        publishableExceptionMessage.setTimestamp(exception.timestamp());
        publishableExceptionMessage.setSerializableInfo(serializableInfo);

        String data = _jsonSerializer.serialize(publishableExceptionMessage);

        return new ProducerRecord<>(
                topicTagData.getTopic(),
                getPartionByRouteKey(routeKey, topicTagData.getTopic()),
                exception.id(),
                data
        );
    }

    private ProducerRecord<String, String> buildCommandMessage(ICommand command, String routeKey, boolean needReply) {
        Ensure.notNull(command.getAggregateRootId(), "aggregateRootId");
        String commandData = _jsonSerializer.serialize(command);
        TopicData topicTagData = _commandTopicProvider.getPublishTopic(command);
        String replyAddress = needReply && _commandResultProcessor != null ? RemotingUtil.parseAddress(_commandResultProcessor.getBindingAddress()) : null;
        String messageData = _jsonSerializer.serialize(new CommandMessage(commandData, replyAddress, command.getClass().getName()));
        //命令唯一id，聚合根id
        String key = String.format("%s%s", command.id(), command.getAggregateRootId() == null ? "" : "cmd_agg_" + command.getAggregateRootId());
        ProducerRecord message = new ProducerRecord<>(topicTagData.getTopic(), getPartionByRouteKey(routeKey, topicTagData.getTopic()), key, messageData);
        return message;
    }

}
