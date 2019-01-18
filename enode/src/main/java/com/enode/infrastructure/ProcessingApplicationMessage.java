package com.enode.infrastructure;

public class ProcessingApplicationMessage implements IProcessingMessage<ProcessingApplicationMessage, IApplicationMessage> {
    public IApplicationMessage message;
    private ProcessingMessageMailbox<ProcessingApplicationMessage, IApplicationMessage> _mailbox;
    private IMessageProcessContext _processContext;

    public ProcessingApplicationMessage(IApplicationMessage message, IMessageProcessContext processContext) {
        this.message = message;
        _processContext = processContext;
    }

    @Override
    public void setMailbox(ProcessingMessageMailbox<ProcessingApplicationMessage, IApplicationMessage> mailbox) {
        _mailbox = mailbox;
    }

    @Override
    public void complete() {
        _processContext.notifyMessageProcessed();
        if (_mailbox != null) {
            _mailbox.completeMessage(this);
        }
    }

    @Override
    public IApplicationMessage getMessage() {
        return message;
    }
}
