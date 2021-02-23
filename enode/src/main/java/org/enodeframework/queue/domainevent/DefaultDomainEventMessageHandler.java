package org.enodeframework.queue.domainevent;

import com.google.common.base.Strings;
import org.enodeframework.common.SysProperties;
import org.enodeframework.common.io.ReplySocketAddress;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.common.utilities.InetUtil;
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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class DefaultDomainEventMessageHandler implements IMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultDomainEventMessageHandler.class);

    private final ISendReplyService sendReplyService;

    private final IEventSerializer eventSerializer;

    private final IProcessingEventProcessor domainEventMessageProcessor;

    private final ISerializeService serializeService;

    private boolean sendEventHandledMessage = true;

    public DefaultDomainEventMessageHandler(ISendReplyService sendReplyService, IProcessingEventProcessor domainEventMessageProcessor, IEventSerializer eventSerializer, ISerializeService serializeService) {
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
        logger.info("Received event stream message: {}", serializeService.serialize(queueMessage));
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
        private final DefaultDomainEventMessageHandler eventConsumer;
        private final DomainEventStreamMessage domainEventStreamMessage;
        private final QueueMessage queueMessage;
        private final IMessageContext messageContext;

        public DomainEventStreamProcessContext(
                DefaultDomainEventMessageHandler eventConsumer,
                DomainEventStreamMessage domainEventStreamMessage,
                QueueMessage queueMessage,
                IMessageContext messageContext) {
            this.eventConsumer = eventConsumer;
            this.domainEventStreamMessage = domainEventStreamMessage;
            this.queueMessage = queueMessage;
            this.messageContext = messageContext;
        }

        @Override
        public CompletableFuture<Boolean> notifyEventProcessed() {
            messageContext.onMessageHandled(queueMessage);
            if (!eventConsumer.isSendEventHandledMessage()) {
                return Task.completedTask;
            }
            String address = (String) domainEventStreamMessage.getItems().getOrDefault(SysProperties.ITEMS_COMMAND_REPLY_ADDRESS_KEY, "");
            if (Strings.isNullOrEmpty(address)) {
                return Task.completedTask;
            }
            ReplySocketAddress replyAddress = InetUtil.toSocketAddress(address);
            if (Objects.isNull(replyAddress)) {
                logger.error("can not parse notify address, {}", domainEventStreamMessage);
                return Task.completedTask;
            }
            String commandResult = (String) domainEventStreamMessage.getItems().getOrDefault(SysProperties.ITEMS_COMMAND_RESULT_KEY, "");
            DomainEventHandledMessage domainEventHandledMessage = new DomainEventHandledMessage();
            domainEventHandledMessage.setCommandId(domainEventStreamMessage.getCommandId());
            domainEventHandledMessage.setAggregateRootId(domainEventStreamMessage.getAggregateRootId());
            domainEventHandledMessage.setCommandResult(commandResult);
            return eventConsumer.getSendReplyService().sendEventReply(domainEventHandledMessage, replyAddress);
        }
    }
}
