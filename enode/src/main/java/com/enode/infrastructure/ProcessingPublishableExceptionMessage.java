package com.enode.infrastructure;

public class ProcessingPublishableExceptionMessage implements IProcessingMessage<ProcessingPublishableExceptionMessage, IPublishableException> {
    private ProcessingMessageMailbox<ProcessingPublishableExceptionMessage, IPublishableException> _mailbox;

    private IMessageProcessContext processContext;

    private IPublishableException message;

    public ProcessingPublishableExceptionMessage(IPublishableException message, IMessageProcessContext processContext) {
        this.message = message;
        this.processContext = processContext;
    }

    @Override
    public void setMailbox(ProcessingMessageMailbox<ProcessingPublishableExceptionMessage, IPublishableException> mailbox) {
        _mailbox = mailbox;
    }

    @Override
    public void complete() {
        processContext.notifyMessageProcessed();
        if (_mailbox != null) {
            _mailbox.completeMessage(this);
        }
    }

    @Override
    public IPublishableException getMessage() {
        return message;
    }
}
