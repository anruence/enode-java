package com.enodeframework.infrastructure;

/**
 * @author anruence@gmail.com
 */
public class ProcessingPublishableExceptionMessage implements IProcessingMessage<ProcessingPublishableExceptionMessage, IPublishableException> {
    private ProcessingMessageMailbox<ProcessingPublishableExceptionMessage, IPublishableException> mailbox;

    private IMessageProcessContext processContext;

    private IPublishableException message;

    public ProcessingPublishableExceptionMessage(IPublishableException message, IMessageProcessContext processContext) {
        this.message = message;
        this.processContext = processContext;
    }

    @Override
    public void setMailbox(ProcessingMessageMailbox<ProcessingPublishableExceptionMessage, IPublishableException> mailbox) {
        this.mailbox = mailbox;
    }

    @Override
    public void complete() {
        processContext.notifyMessageProcessed();
        if (mailbox != null) {
            mailbox.completeMessage(this);
        }
    }

    @Override
    public IPublishableException getMessage() {
        return message;
    }
}
