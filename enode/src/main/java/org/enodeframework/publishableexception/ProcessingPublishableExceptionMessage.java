package org.enodeframework.publishableexception;

import org.enodeframework.messaging.IEventProcessContext;

/**
 * @author anruence@gmail.com
 */
public class ProcessingPublishableExceptionMessage {
    private IEventProcessContext processContext;
    private IPublishableException message;

    public ProcessingPublishableExceptionMessage(IPublishableException message, IEventProcessContext processContext) {
        this.message = message;
        this.processContext = processContext;
    }

    public void complete() {
        processContext.notifyEventProcessed();
    }

    public IPublishableException getMessage() {
        return message;
    }
}
