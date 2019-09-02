package com.enodeframework.eventing;

import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.eventing.ProcessingDomainEventStreamMessageMailBox;
import com.enodeframework.messaging.IMessageProcessContext;

/**
 * @author anruence@gmail.com
 */
public class ProcessingDomainEventStreamMessage {
    private DomainEventStreamMessage message;
    private ProcessingDomainEventStreamMessageMailBox mailbox;
    private IMessageProcessContext processContext;

    public ProcessingDomainEventStreamMessage(DomainEventStreamMessage message, IMessageProcessContext processContext) {
        this.message = message;
        this.processContext = processContext;
    }

    public ProcessingDomainEventStreamMessageMailBox getMailbox() {
        return mailbox;
    }

    public void setMailbox(ProcessingDomainEventStreamMessageMailBox mailbox) {
        this.mailbox = mailbox;
    }

    public void complete() {
        processContext.notifyMessageProcessed();
        if (mailbox != null) {
            mailbox.completeRun();
        }
    }

    public DomainEventStreamMessage getMessage() {
        return message;
    }
}
