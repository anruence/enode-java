package com.enodeframework.infrastructure;

import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.eventing.DomainEventStreamMessage;

/**
 * @author anruence@gmail.com
 */
public class ProcessingDomainEventStreamMessage implements IProcessingMessage<ProcessingDomainEventStreamMessage, DomainEventStreamMessage>, ISequenceProcessingMessage {
    public DomainEventStreamMessage message;
    private ProcessingMessageMailbox<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> mailbox;
    private IMessageProcessContext processContext;

    public ProcessingDomainEventStreamMessage(DomainEventStreamMessage message, IMessageProcessContext processContext) {
        this.message = message;
        this.processContext = processContext;
    }

    @Override
    public void setMailbox(ProcessingMessageMailbox<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> mailbox) {
        this.mailbox = mailbox;
    }

    @Override
    public void addToWaitingList() {
        Ensure.notNull(mailbox, "mailbox");
        mailbox.addWaitingForRetryMessage(this);
    }

    @Override
    public void complete() {
        processContext.notifyMessageProcessed();
        if (mailbox != null) {
            mailbox.completeMessage(this);
        }
    }

    @Override
    public DomainEventStreamMessage getMessage() {
        return message;
    }
}
