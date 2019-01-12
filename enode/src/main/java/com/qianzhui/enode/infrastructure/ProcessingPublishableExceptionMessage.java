package com.qianzhui.enode.infrastructure;

import javax.inject.Inject;

public class ProcessingPublishableExceptionMessage implements IProcessingMessage<ProcessingPublishableExceptionMessage, IPublishableException> {
    private ProcessingMessageMailbox<ProcessingPublishableExceptionMessage, IPublishableException> _mailbox;
    private IMessageProcessContext _processContext;

    private IPublishableException message;

    @Inject
    public ProcessingPublishableExceptionMessage(IPublishableException message, IMessageProcessContext processContext) {
        this.message = message;
        _processContext = processContext;
    }

    @Override
    public void setMailbox(ProcessingMessageMailbox<ProcessingPublishableExceptionMessage, IPublishableException> mailbox) {
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
    public IPublishableException getMessage() {
        return message;
    }
}
