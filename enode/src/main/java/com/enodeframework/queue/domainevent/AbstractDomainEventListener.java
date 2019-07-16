package com.enodeframework.queue.domainevent;

import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.eventing.IDomainEvent;
import com.enodeframework.eventing.IEventSerializer;
import com.enodeframework.infrastructure.IMessageProcessor;
import com.enodeframework.infrastructure.ProcessingDomainEventStreamMessage;
import com.enodeframework.infrastructure.impl.DefaultMessageProcessContext;
import com.enodeframework.queue.IMessageContext;
import com.enodeframework.queue.IMessageHandler;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.SendReplyService;
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
    protected IMessageProcessor<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> domainEventMessageProcessor;
    protected boolean sendEventHandledMessage = true;

    public SendReplyService getSendReplyService() {
        return sendReplyService;
    }

    public boolean isSendEventHandledMessage() {
        return sendEventHandledMessage;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        EventStreamMessage message = JsonTool.deserialize(queueMessage.getBody(), EventStreamMessage.class);
        DomainEventStreamMessage domainEventStreamMessage = convertToDomainEventStream(message);
        DomainEventStreamProcessContext processContext = new DomainEventStreamProcessContext(AbstractDomainEventListener.this, domainEventStreamMessage, queueMessage, context);
        ProcessingDomainEventStreamMessage processingMessage = new ProcessingDomainEventStreamMessage(domainEventStreamMessage, processContext);
        if (logger.isDebugEnabled()) {
            logger.debug("ENode event message received, messageId: {}, aggregateRootId: {}, aggregateRootType: {}, version: {}", domainEventStreamMessage.id(), domainEventStreamMessage.aggregateRootStringId(), domainEventStreamMessage.aggregateRootTypeName(), domainEventStreamMessage.version());
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

    class DomainEventStreamProcessContext extends DefaultMessageProcessContext {
        private final AbstractDomainEventListener eventConsumer;
        private final DomainEventStreamMessage domainEventStreamMessage;

        public DomainEventStreamProcessContext(
                AbstractDomainEventListener eventConsumer, DomainEventStreamMessage domainEventStreamMessage,
                QueueMessage queueMessage, IMessageContext messageContext) {
            super(queueMessage, messageContext);
            this.eventConsumer = eventConsumer;
            this.domainEventStreamMessage = domainEventStreamMessage;
        }

        @Override
        public void notifyMessageProcessed() {
            super.notifyMessageProcessed();
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
            domainEventHandledMessage.setAggregateRootId(domainEventStreamMessage.aggregateRootId());
            domainEventHandledMessage.setCommandResult(commandResult);
            eventConsumer.getSendReplyService().sendReply(CommandReturnType.EventHandled.getValue(), domainEventHandledMessage, replyAddress);
        }
    }

}
