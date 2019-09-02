package com.enodeframework.eventing;

import com.enodeframework.messaging.Message;

/**
 * Represents an abstract generic domain event.
 */
public abstract class DomainEvent<TAggregateRootId> extends Message implements IDomainEvent<TAggregateRootId> {

    private String commandId;
    private TAggregateRootId aggregateRootId;
    private String aggregateRootStringId;
    private String aggregateRootTypeName;
    private int version;
    private int sequence;

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }


    @Override
    public TAggregateRootId getAggregateRootId() {
        return aggregateRootId;
    }

    @Override
    public void setAggregateRootId(TAggregateRootId aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }

    @Override
    public String getAggregateRootStringId() {
        return aggregateRootStringId;
    }

    @Override
    public void setAggregateRootStringId(String aggregateRootStringId) {
        this.aggregateRootStringId = aggregateRootStringId;
    }

    @Override
    public String getAggregateRootTypeName() {
        return aggregateRootTypeName;
    }


    @Override
    public void setAggregateRootTypeName(String aggregateRootTypeName) {
        this.aggregateRootTypeName = aggregateRootTypeName;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }
}
