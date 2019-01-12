package com.qianzhui.enode.infrastructure;

import com.qianzhui.enode.common.utilities.Ensure;
import com.qianzhui.enode.eventing.DomainEventStreamMessage;

public class ProcessingDomainEventStreamMessage implements IProcessingMessage<ProcessingDomainEventStreamMessage, DomainEventStreamMessage>, ISequenceProcessingMessage {
    private ProcessingMessageMailbox<ProcessingDomainEventStreamMessage, DomainEventStreamMessage> _mailbox;
    private IMessageProcessContext _processContext;

    public DomainEventStreamMessage message;

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
