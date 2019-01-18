package com.enode.commanding;

import com.enode.infrastructure.Message;

public class Command<TAggregateRootId> extends Message implements ICommand {

    public TAggregateRootId aggregateRootId;

    public Command() {
        super();
    }

    public Command(TAggregateRootId aggregateRootId) {
        super();
        if (aggregateRootId == null) {
            throw new NullPointerException("aggregateRootId");
        }
        this.aggregateRootId = aggregateRootId;
    }

    @Override
    public String getAggregateRootId() {
        if (this.aggregateRootId != null) {
            return this.aggregateRootId.toString();
        } else {
            return null;
        }
    }

    @Override
    public String getRoutingKey() {
        return getAggregateRootId();
    }
}
