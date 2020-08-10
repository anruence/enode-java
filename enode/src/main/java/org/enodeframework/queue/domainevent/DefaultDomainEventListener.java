package org.enodeframework.queue.domainevent;

import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.IEventProcessContext;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.eventing.IProcessingEventProcessor;
import org.enodeframework.eventing.ProcessingEvent;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.ISendReplyService;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultDomainEventListener implements IMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDomainEventListener.class);

    private final ISendReplyService sendReplyService;

    private final IEventSerializer eventSerializer;

    private final IProcessingEventProcessor domainEventMessageProcessor;
    private final ISerializeService serializeService;

    private boolean sendEventHandledMessage = true;

    public DefaultDomainEventListener(ISendReplyService sendReplyService, IProcessingEventProcessor domainEventMessageProcessor, IEventSerializer eventSerializer, ISerializeService serializeService) {
        this.sendReplyService = sendReplyService;
        this.eventSerializer = eventSerializer;
        this.domainEventMessageProcessor = domainEventMessageProcessor;
        this.serializeService = serializeService;
    }

    public ISendReplyService getSendReplyService() {
        return sendReplyService;
    }

    @Override
    public void handle(QueueMessage queueMessage, IMessageContext context) {
        logger.info("Received event stream message: {}", queueMessage);
        EventStreamMessage message = serializeService.deserialize(queueMessage.getBody(), EventStreamMessage.class);
        DomainEventStreamMessage domainEventStreamMessage = convertToDomainEventStream(message);
        DomainEventStreamProcessContext processContext = new DomainEventStreamProcessContext(this, domainEventStreamMessage, queueMessage, context);
        ProcessingEvent processingMessage = new ProcessingEvent(domainEventStreamMessage, processContext);
        domainEventMessageProcessor.process(processingMessage);
    }

    private DomainEventStreamMessage convertToDomainEventStream(EventStreamMessage message) {
        DomainEventStreamMessage domainEventStreamMessage = new DomainEventStreamMessage(
                message.getCommandId(),
                message.getAggregateRootId(),
                message.getVersion(),
                message.getAggregateRootTypeName(),
                eventSerializer.deserialize(message.getEvents()),
                message.getItems()
        );
        domainEventStreamMessage.setId(message.getId());
        domainEventStreamMessage.setTimestamp(message.getTimestamp());
        return domainEventStreamMessage;
    }

    public boolean isSendEventHandledMessage() {
        return sendEventHandledMessage;
    }

    public void setSendEventHandledMessage(boolean sendEventHandledMessage) {
        this.sendEventHandledMessage = sendEventHandledMessage;
    }

    static class DomainEventStreamProcessContext implements IEventProcessContext {
        private final DefaultDomainEventListener eventConsumer;
        private final DomainEventStreamMessage domainEventStreamMessage;
        private final QueueMessage queueMessage;
        private final IMessageContext messageContext;

        public DomainEventStreamProcessContext(
                DefaultDomainEventListener eventConsumer,
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
            eventConsumer.getSendReplyService().sendEventReply(domainEventHandledMessage, replyAddress);
        }
    }
}
