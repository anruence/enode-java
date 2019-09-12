package org.enodeframework.queue.domainevent;

import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.IDomainEvent;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.eventing.IProcessingEventProcessor;
import org.enodeframework.eventing.ProcessingEvent;
import org.enodeframework.messaging.IEventProcessContext;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.SendReplyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDomainEventListener implements IMessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractDomainEventListener.class);
    @Autowired
    protected SendReplyService sendReplyService;
    @Autowired
    protected IEventSerializer eventSerializer;
    @Autowired
    protected IProcessingEventProcessor domainEventMessageProcessor;
    protected boolean sendEventHandledMessage = true;

    public AbstractDomainEventListener setEventSerializer(IEventSerializer eventSerializer) {
        this.eventSerializer = eventSerializer;
        return this;
    }

    public AbstractDomainEventListener setDomainEventMessageProcessor(IProcessingEventProcessor domainEventMessageProcessor) {
        this.domainEventMessageProcessor = domainEventMessageProcessor;
        return this;
    }

    public SendReplyService getSendReplyService() {
        return sendReplyService;
    }

    public AbstractDomainEventListener setSendReplyService(SendReplyService sendReplyService) {
        this.sendReplyService = sendReplyService;
        return this;
    }

    public boolean isSendEventHandledMessage() {
        return sendEventHandledMessage;
    }

    public AbstractDomainEventListener setSendEventHandledMessage(boolean sendEventHandledMessage) {
        this.sendEventHandledMessage = sendEventHandledMessage;
        return this;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        EventStreamMessage message = JsonTool.deserialize(queueMessage.getBody(), EventStreamMessage.class);
        DomainEventStreamMessage domainEventStreamMessage = convertToDomainEventStream(message);
        DomainEventStreamProcessContext processContext = new DomainEventStreamProcessContext(this, domainEventStreamMessage, queueMessage, context);
        ProcessingEvent processingMessage = new ProcessingEvent(domainEventStreamMessage, processContext);
        if (logger.isDebugEnabled()) {
            logger.debug("ENode event stream message received, messageId: {}, aggregateRootId: {}, aggregateRootType: {}, version: {}", domainEventStreamMessage.getId(), domainEventStreamMessage.getAggregateRootId(), domainEventStreamMessage.getAggregateRootTypeName(), domainEventStreamMessage.getVersion());
        }
        domainEventMessageProcessor.process(processingMessage);
    }

    private DomainEventStreamMessage convertToDomainEventStream(EventStreamMessage message) {
        DomainEventStreamMessage domainEventStreamMessage = new DomainEventStreamMessage(
                message.getCommandId(),
                message.getAggregateRootId(),
                message.getVersion(),
                message.getAggregateRootTypeName(),
                eventSerializer.deserialize(message.getEvents(), IDomainEvent.class),
                message.getItems()
        );
        domainEventStreamMessage.setId(message.getId());
        domainEventStreamMessage.setTimestamp(message.getTimestamp());
        return domainEventStreamMessage;
    }

    class DomainEventStreamProcessContext implements IEventProcessContext {
        private final AbstractDomainEventListener eventConsumer;
        private final DomainEventStreamMessage domainEventStreamMessage;
        private final QueueMessage queueMessage;
        private final IMessageContext messageContext;

        public DomainEventStreamProcessContext(
                AbstractDomainEventListener eventConsumer,
                DomainEventStreamMessage domainEventStreamMessage,
                QueueMessage queueMessage,
                IMessageContext messageContext) {
            this.eventConsumer = eventConsumer;
            this.domainEventStreamMessage = domainEventStreamMessage;
            this.queueMessage = queueMessage;
            this.messageContext = messageContext;
        }

        @Override
        public void notifyEventProcessed() {
            messageContext.onMessageHandled(queueMessage);
            if (!eventConsumer.isSendEventHandledMessage()) {
                return;
            }
            String replyAddress = domainEventStreamMessage.getItems().get("CommandReplyAddress");
            if (replyAddress == null || "".equals(replyAddress.trim())) {
                return;
            }
            String commandResult = domainEventStreamMessage.getItems().get("CommandResult");
            DomainEventHandledMessage domainEventHandledMessage = new DomainEventHandledMessage();
            domainEventHandledMessage.setCommandId(domainEventStreamMessage.getCommandId());
            domainEventHandledMessage.setAggregateRootId(domainEventStreamMessage.getAggregateRootId());
            domainEventHandledMessage.setCommandResult(commandResult);
            eventConsumer.getSendReplyService().sendReply(CommandReturnType.EventHandled.getValue(), domainEventHandledMessage, replyAddress);
        }
    }
}
