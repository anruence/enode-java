package com.enodeframework.eventing;

import com.enodeframework.commanding.ProcessingCommand;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.infrastructure.IMailBox;
import com.enodeframework.infrastructure.IMailBoxMessage;

/**
 * @author anruence@gmail.com
 */
public class EventCommittingContext implements IMailBoxMessage<EventCommittingContext, Boolean> {
    private IMailBox<EventCommittingContext, Boolean> mailBox;
    private long sequence;
    private IAggregateRoot aggregateRoot;
    private DomainEventStream eventStream;
    private ProcessingCommand processingCommand;
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

    public EventCommittingContext getNext() {
        return next;
    }

    public void setNext(EventCommittingContext next) {
        this.next = next;
    }

    @Override
    public IMailBox<EventCommittingContext, Boolean> getMailBox() {
        return mailBox;
    }

    @Override
    public void setMailBox(IMailBox<EventCommittingContext, Boolean> mailBox) {
        this.mailBox = mailBox;
    }

    @Override
    public long getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
}
