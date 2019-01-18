package com.enode.infrastructure;

import com.enode.common.utilities.Ensure;
import com.enode.eventing.DomainEventStreamMessage;

public class ProcessingDomainEventStreamMessage implements IProcessingMessage<ProcessingDomainEventStreamMessage, DomainEventStreamMessage>, ISequenceProcessingMessage {
    public DomainEventStreamMessage message;
    private ProcessingMessageMailbox<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> _mailbox;
    private IMessageProcessContext _processContext;

    public ProcessingDomainEventStreamMessage(DomainEventStreamMessage message, IMessageProcessContext processContext) {
        this.message = message;
        _processContext = processContext;
    }

    @Override
    public void setMailbox(ProcessingMessageMailbox<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> mailbox) {
        _mailbox = mailbox;
    }

    @Override
    public void addToWaitingList() {
        Ensure.notNull(_mailbox, "_mailbox");
        _mailbox.addWaitingForRetryMessage(this);
    }

    @Override
    public void complete() {
        _processContext.notifyMessageProcessed();
        if (_mailbox != null) {
            _mailbox.completeMessage(this);
        }
    }

    @Override
    public DomainEventStreamMessage getMessage() {
        return message;
    }
}
