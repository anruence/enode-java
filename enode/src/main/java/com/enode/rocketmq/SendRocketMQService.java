package com.enode.rocketmq;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageConst;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.enode.commanding.ICommand;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.remoting.common.RemotingUtil;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.utilities.BitConverter;
import com.enode.common.utilities.Ensure;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.eventing.IEventSerializer;
import com.enode.infrastructure.IApplicationMessage;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.IPublishableException;
import com.enode.infrastructure.ISequenceMessage;
import com.enode.rocketmq.applicationmessage.ApplicationDataMessage;
import com.enode.rocketmq.client.Producer;
import com.enode.rocketmq.command.CommandMessage;
import com.enode.rocketmq.command.CommandResultProcessor;
import com.enode.rocketmq.domainevent.EventStreamMessage;
import com.enode.rocketmq.publishableexceptions.PublishableExceptionMessage;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SendRocketMQService implements IMQProducer {
    private static final Logger logger = ENodeLogger.getLog();

    private IJsonSerializer _jsonSerializer;
    private ITopicProvider<ICommand> _commandTopicProvider;
    private ITopicProvider<IApplicationMessage> _messageTopicProvider;
    private ITopicProvider<IPublishableException> _exceptionTopicProvider;
    private ITopicProvider<IDomainEvent> _eventTopicProvider;
    private IEventSerializer _eventSerializer;
    private CommandResultProcessor _commandResultProcessor;
    private Producer _producer;

    @Inject
    public SendRocketMQService(
            IJsonSerializer jsonSerializer,
            ITopicProvider<ICommand> commandTopicProvider,
            ITopicProvider<IApplicationMessage> messageTopicProvider,
            ITopicProvider<IPublishableException> exceptionTopicProvider,
            ITopicProvider<IDomainEvent> eventTopicProvider,
            IEventSerializer eventSerializer,
            CommandResultProcessor commandResultProcessor,
            Producer producer
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

    private MessageQueue messageQueueSelect(List<MessageQueue> queues, Message msg, Object routingKey) {
        int hash = Math.abs(routingKey.hashCode());
        return queues.get(hash % queues.size());
    }

    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(IMessage msg, String routingKey) {
        return sendAsync(msg, routingKey, false);
    }

    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(IMessage msg, String routingKey, boolean sendReply) {
        CompletableFuture<AsyncTaskResult> promise = new CompletableFuture<>();
        Message message = null;
        if (msg instanceof ICommand) {
            ICommand commandMessage = (ICommand) msg;
            message = buildCommandMessage(commandMessage, sendReply);
        }
        if (msg instanceof IApplicationMessage) {
            IApplicationMessage applicationMessage = (IApplicationMessage) msg;
            message = createApplicationMessage(applicationMessage);
        }
        if (msg instanceof PublishableExceptionMessage) {
            IPublishableException exceptionMessage = (IPublishableException) msg;
            message = createExecptionMessage(exceptionMessage);
        }
        if (msg instanceof DomainEventStreamMessage) {
            DomainEventStreamMessage eventStreamMessage = (DomainEventStreamMessage) msg;
            message = createDomainEventStreamMessage(eventStreamMessage);
        }
        if (message == null) {
            promise.complete(new AsyncTaskResult(AsyncTaskStatus.Failed, "Message return null"));
            return promise;
        }
        _producer.send(message, this::messageQueueSelect, routingKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                promise.complete(AsyncTaskResult.Success);
            }

            @Override
            public void onException(Throwable ex) {
                promise.complete(new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage()));
            }
        });
        return promise;
    }

    @Override
    public void start() {
        _producer.start();
    }

    @Override
    public void shutdown() {
        _producer.shutdown();
    }

    private Message createDomainEventStreamMessage(DomainEventStreamMessage eventStream) {
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
        byte[] body = BitConverter.getBytes(data);
        return new Message(topicTagData.getTopic(), topicTagData.getTag(), key,
                QueueMessageTypeCode.DomainEventStreamMessage.getValue(), body, true);
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

    private Message createApplicationMessage(IApplicationMessage message) {
        TopicData topicTagData = _messageTopicProvider.getPublishTopic(message);
        String appMessageData = _jsonSerializer.serialize(message);
        ApplicationDataMessage appDataMessage = new ApplicationDataMessage(appMessageData, message.getClass().getName());
        String data = _jsonSerializer.serialize(appDataMessage);
        return new Message(topicTagData.getTopic(),
                topicTagData.getTag(),
                message.id(),
                QueueMessageTypeCode.ApplicationMessage.getValue(),
                BitConverter.getBytes(data),
                true);
    }

    private Message createExecptionMessage(IPublishableException exception) {
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

        return new Message(topicTagData.getTopic(),
                topicTagData.getTag(),
                exception.id(),
                QueueMessageTypeCode.ExceptionMessage.getValue(),
                BitConverter.getBytes(data),
                true);
    }

    private Message buildCommandMessage(ICommand command, boolean needReply) {
        Ensure.notNull(command.getAggregateRootId(), "aggregateRootId");
        String commandData = _jsonSerializer.serialize(command);
        TopicData topicTagData = _commandTopicProvider.getPublishTopic(command);
        String replyAddress = needReply && _commandResultProcessor != null ? RemotingUtil.parseAddress(_commandResultProcessor.getBindingAddress()) : null;
        String messageData = _jsonSerializer.serialize(new CommandMessage(commandData, replyAddress, command.getClass().getName()));
        byte[] body = BitConverter.getBytes(messageData);
        //命令唯一id，聚合根id
        String key = String.format("%s%s", command.id(),
                command.getAggregateRootId() == null ? "" : MessageConst.KEY_SEPARATOR + "cmd_agg_" + command.getAggregateRootId());
        Message message = new Message(topicTagData.getTopic(),
                topicTagData.getTag(),
                key,
                QueueMessageTypeCode.CommandMessage.getValue(), body, true);

        message.putUserProperty(RocketMQSystemPropKey.STARTDELIVERTIME, String.valueOf(command.getRoutingKey()));
        return message;
    }

    static public class RocketMQSystemPropKey {
        public static final String TAG = "__TAG";
        public static final String KEY = "__KEY";
        public static final String MSGID = "__MSGID";
        public static final String RECONSUMETIMES = "__RECONSUMETIMES";
        /**
         * 设置消息的定时投递时间（绝对时间),最大延迟时间为7天.
         * <p>例1: 延迟投递, 延迟3s投递, 设置为: System.currentTimeMillis() + 3000;
         * <p>例2: 定时投递, 2016-02-01 11:30:00投递, 设置为: new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-02-01 11:30:00").getTime()
         */
        public static final String STARTDELIVERTIME = "__STARTDELIVERTIME";
    }
}
