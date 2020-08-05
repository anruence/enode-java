package org.enodeframework.eventing;

import org.enodeframework.commanding.ProcessingCommand;

/**
 * @author anruence@gmail.com
 */
public class EventCommittingContext {
    private EventCommittingContextMailBox mailBox;
    private DomainEventStream eventStream;
    private ProcessingCommand processingCommand;

    public EventCommittingContext(DomainEventStream eventStream, ProcessingCommand processingCommand) {
        this.eventStream = eventStream;
        this.processingCommand = processingCommand;
    }

    public DomainEventStream getEventStream() {
        return eventStream;
    }

    public ProcessingCommand getProcessingCommand() {
        return processingCommand;
    }

    public EventCommittingContextMailBox getMailBox() {
        return mailBox;
    }

    public void setMailBox(EventCommittingContextMailBox mailBox) {
        this.mailBox = mailBox;
    }
}
