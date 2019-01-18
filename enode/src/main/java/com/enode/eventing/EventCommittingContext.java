package com.enode.eventing;

import com.enode.commanding.ProcessingCommand;
import com.enode.domain.IAggregateRoot;
import com.enode.eventing.impl.EventMailBox;

public class EventCommittingContext {
    private IAggregateRoot aggregateRoot;
    private DomainEventStream eventStream;
    private ProcessingCommand processingCommand;
    private EventMailBox eventMailBox;
    private EventCommittingContext next;

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

    public EventMailBox getEventMailBox() {
        return eventMailBox;
    }

    public void setEventMailBox(EventMailBox eventMailBox) {
        this.eventMailBox = eventMailBox;
    }

    public EventCommittingContext getNext() {
        return next;
    }

    public void setNext(EventCommittingContext next) {
        this.next = next;
    }
}
