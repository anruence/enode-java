package com.enodeframework.publishableexception;

import com.enodeframework.messaging.IMessageProcessContext;

/**
 * @author anruence@gmail.com
 */
public class ProcessingPublishableExceptionMessage {
    private IMessageProcessContext processContext;
    private IPublishableException message;

    public ProcessingPublishableExceptionMessage(IPublishableException message, IMessageProcessContext processContext) {
        this.message = message;
        this.processContext = processContext;
    }

    public void complete() {
        processContext.notifyMessageProcessed();
    }

    public IPublishableException getMessage() {
        return message;
    }
}
