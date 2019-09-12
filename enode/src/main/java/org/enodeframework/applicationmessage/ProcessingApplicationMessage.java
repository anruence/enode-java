package org.enodeframework.applicationmessage;

import org.enodeframework.messaging.IEventProcessContext;

/**
 * @author anruence@gmail.com
 */
public class ProcessingApplicationMessage {
    public IApplicationMessage message;
    private IEventProcessContext processContext;

    public ProcessingApplicationMessage(IApplicationMessage message, IEventProcessContext processContext) {
        this.message = message;
        this.processContext = processContext;
    }

    public void complete() {
        processContext.notifyEventProcessed();
    }

    public IApplicationMessage getMessage() {
        return message;
    }
}
