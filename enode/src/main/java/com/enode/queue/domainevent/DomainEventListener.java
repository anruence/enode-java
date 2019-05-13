package com.enode.queue.domainevent;

import com.enode.commanding.CommandReturnType;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IDomainEvent;
import com.enode.eventing.IEventSerializer;
import com.enode.infrastructure.IMessageProcessor;
import com.enode.infrastructure.ProcessingDomainEventStreamMessage;
import com.enode.infrastructure.impl.DefaultMessageProcessContext;
import com.enode.queue.IMessageContext;
import com.enode.queue.IMessageHandler;
import com.enode.queue.QueueMessage;
import com.enode.queue.SendReplyService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class DomainEventListener implements IMessageHandler {

    private static final Logger _logger = ENodeLogger.getLog();

    @Autowired
    protected SendReplyService _sendReplyService;

    @Autowired
    protected IJsonSerializer _jsonSerializer;

    @Autowired
    protected IEventSerializer _eventSerializer;

    @Autowired
    protected IMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> _processor;

    protected boolean _sendEventHandledMessage = true;

    public SendReplyService getSendReplyService() {
        return _sendReplyService;
    }

    public boolean isSendEventHandledMessage() {
        return _sendEventHandledMessage;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        EventStreamMessage message = _jsonSerializer.deserialize(queueMessage.getBody(), EventStreamMessage.class);
        DomainEventStreamMessage domainEventStreamMessage = convertToDomainEventStream(message);
        DomainEventStreamProcessContext processContext = new DomainEventStreamProcessContext(DomainEventListener.this, domainEventStreamMessage, queueMessage, context);
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

    class DomainEventStreamProcessContext extends DefaultMessageProcessContext {
        private final DomainEventListener _eventConsumer;
        private final DomainEventStreamMessage _domainEventStreamMessage;

        public DomainEventStreamProcessContext(
                DomainEventListener eventConsumer, DomainEventStreamMessage domainEventStreamMessage,
                QueueMessage queueMessage, IMessageContext messageContext) {
            super(queueMessage, messageContext);
            _eventConsumer = eventConsumer;
            _domainEventStreamMessage = domainEventStreamMessage;
        }

        @Override
        public void notifyMessageProcessed() {
            super.notifyMessageProcessed();
            if (!_eventConsumer.isSendEventHandledMessage()) {
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
            _eventConsumer.getSendReplyService().sendReply(CommandReturnType.EventHandled.getValue(), domainEventHandledMessage, replyAddress);
        }
    }

}
