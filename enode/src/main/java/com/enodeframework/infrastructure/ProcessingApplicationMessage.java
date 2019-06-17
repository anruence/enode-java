package com.enodeframework.infrastructure;

/**
 * @author anruence@gmail.com
 */
public class ProcessingApplicationMessage implements IProcessingMessage<ProcessingApplicationMessage, IApplicationMessage> {
    public IApplicationMessage message;
    private ProcessingMessageMailbox<ProcessingApplicationMessage, IApplicationMessage> mailbox;
    private IMessageProcessContext processContext;

    public ProcessingApplicationMessage(IApplicationMessage message, IMessageProcessContext processContext) {
        this.message = message;
        this.processContext = processContext;
    }

    @Override
    public void setMailbox(ProcessingMessageMailbox<ProcessingApplicationMessage, IApplicationMessage> mailbox) {
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
    public IApplicationMessage getMessage() {
        return message;
    }
}
