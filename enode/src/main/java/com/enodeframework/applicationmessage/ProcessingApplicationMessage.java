package com.enodeframework.applicationmessage;

import com.enodeframework.messaging.IMessageProcessContext;

/**
 * @author anruence@gmail.com
 */
public class ProcessingApplicationMessage {
    public IApplicationMessage message;
    private IMessageProcessContext processContext;

    public ProcessingApplicationMessage(IApplicationMessage message, IMessageProcessContext processContext) {
        this.message = message;
        this.processContext = processContext;
    }

    public void complete() {
        processContext.notifyMessageProcessed();
    }

    public IApplicationMessage getMessage() {
        return message;
    }
}
