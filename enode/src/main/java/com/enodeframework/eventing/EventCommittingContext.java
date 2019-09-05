package com.enodeframework.eventing;

import com.enodeframework.commanding.ProcessingCommand;
import com.enodeframework.domain.IAggregateRoot;

/**
 * @author anruence@gmail.com
 */
public class EventCommittingContext {
    private EventCommittingContextMailBox mailBox;
    private IAggregateRoot aggregateRoot;
    private DomainEventStream eventStream;
    private ProcessingCommand processingCommand;

    public EventCommittingContext(IAggregateRoot aggregateRoot, DomainEventStream eventStream, ProcessingCommand processingCommand) {
        this.aggregateRoot = aggregateRoot;
        this.eventStream = eventStream;
        this.processingCommand = processingCommand;
    }

    public IAggregateRoot getAggregateRoot() {
        return aggregateRoot;
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
