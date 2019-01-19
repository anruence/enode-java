package com.enode.rocketmq.domainevent;

import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.utilities.BitConverter;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.eventing.IEventSerializer;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.ProcessingDomainEventStreamMessage;
import com.enode.commanding.CommandReturnType;
import com.enode.rocketmq.SendReplyService;
import com.enode.rocketmq.ITopicProvider;
import com.enode.rocketmq.client.RocketMQConsumer;
import com.enode.rocketmq.client.IMQMessageHandler;
import com.enode.rocketmq.RocketMQProcessContext;
import com.enode.rocketmq.TopicTagData;
import com.enode.rocketmq.consumer.listener.CompletableConsumeConcurrentlyContext;
import org.slf4j.Logger;

import javax.inject.Inject;

public class DomainEventConsumer {
    private static final Logger _logger = ENodeLogger.getLog();

    private final RocketMQConsumer _consumer;
    private final SendReplyService _sendReplyService;
    private final IJsonSerializer _jsonSerializer;
    private final IEventSerializer _eventSerializer;
    private final IMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> _processor;
    private final boolean _sendEventHandledMessage;
    private final ITopicProvider<IDomainEvent> _eventTopicProvider;

    @Inject
    public DomainEventConsumer(
            RocketMQConsumer rocketMQConsumer, IJsonSerializer jsonSerializer, IEventSerializer eventSerializer,
            IMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> processor,
            ITopicProvider<IDomainEvent> eventITopicProvider,
            SendReplyService sendReplyService) {
        _consumer = rocketMQConsumer;
        _sendReplyService = sendReplyService;
        _jsonSerializer = jsonSerializer;
        _eventSerializer = eventSerializer;
        _processor = processor;
        _sendEventHandledMessage = true;
        _eventTopicProvider = eventITopicProvider;
    }

    public DomainEventConsumer start() {
        _consumer.registerMessageHandler(new IMQMessageHandler() {
            @Override
            public boolean isMatched(TopicTagData topicTagData) {
                return _eventTopicProvider.getAllSubscribeTopics().contains(topicTagData);
            }

            @Override
            public void handle(MessageExt message, CompletableConsumeConcurrentlyContext context) {
                DomainEventConsumer.this.handle(message, context);
            }
        });

        if (_sendEventHandledMessage) {
            _sendReplyService.start();
        }
        return this;
    }

    public DomainEventConsumer shutdown() {
        if (_sendEventHandledMessage) {
            _sendReplyService.stop();
        }
        return this;
    }

    void handle(final MessageExt msg,
                final CompletableConsumeConcurrentlyContext context) {
        EventStreamMessage message = _jsonSerializer.deserialize(BitConverter.toString(msg.getBody()), EventStreamMessage.class);

        DomainEventStreamMessage domainEventStreamMessage = convertToDomainEventStream(message);
        DomainEventStreamProcessContext processContext = new DomainEventStreamProcessContext(this, domainEventStreamMessage, msg, context);
        ProcessingDomainEventStreamMessage processingMessage = new ProcessingDomainEventStreamMessage(domainEventStreamMessage, processContext);
        _logger.info("ENode event message received, messageId: {}, aggregateRootId: {}, aggregateRootType: {}, version: {}", domainEventStreamMessage.id(), domainEventStreamMessage.aggregateRootStringId(), domainEventStreamMessage.aggregateRootTypeName(), domainEventStreamMessage.version());
        _processor.process(processingMessage);
    }

    private DomainEventStreamMessage convertToDomainEventStream(EventStreamMessage message) {
        DomainEventStreamMessage domainEventStreamMessage = new DomainEventStreamMessage(
                message.getCommandId(),
                message.getAggregateRootId(),
                message.getVersion(),
                message.getAggregateRootTypeName(),
                _eventSerializer.deserialize(message.getEvents(), IDomainEvent.class),
                message.getItems()
        );
        domainEventStreamMessage.setId(message.getId());
        domainEventStreamMessage.setTimestamp(message.getTimestamp());

        return domainEventStreamMessage;
    }

    class DomainEventStreamProcessContext extends RocketMQProcessContext {
        private final DomainEventConsumer _eventConsumer;
        private final DomainEventStreamMessage _domainEventStreamMessage;

        public DomainEventStreamProcessContext(DomainEventConsumer eventConsumer, DomainEventStreamMessage domainEventStreamMessage,
                                               MessageExt queueMessage, CompletableConsumeConcurrentlyContext messageContext) {
            super(queueMessage, messageContext);
            _eventConsumer = eventConsumer;
            _domainEventStreamMessage = domainEventStreamMessage;
        }

        @Override
        public void notifyMessageProcessed() {
            super.notifyMessageProcessed();

            if (!_eventConsumer._sendEventHandledMessage) {
                return;
            }

            String replyAddress = _domainEventStreamMessage.getItems().get("CommandReplyAddress");
            if (replyAddress == null || "".equals(replyAddress.trim())) {
                return;
            }

            String commandResult = _domainEventStreamMessage.getItems().get("CommandResult");

            DomainEventHandledMessage domainEventHandledMessage = new DomainEventHandledMessage();
            domainEventHandledMessage.setCommandId(_domainEventStreamMessage.getCommandId());
            domainEventHandledMessage.setAggregateRootId(_domainEventStreamMessage.aggregateRootId());
            domainEventHandledMessage.setCommandResult(commandResult);

            _eventConsumer._sendReplyService.sendReply(CommandReturnType.EventHandled.getValue(), domainEventHandledMessage, replyAddress);
        }
    }
}
