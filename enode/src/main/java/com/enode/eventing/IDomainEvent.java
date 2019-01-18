package com.enode.eventing;

import com.enode.infrastructure.ISequenceMessage;

public interface IDomainEvent<TAggregateRootId> extends ISequenceMessage {
    TAggregateRootId aggregateRootId();

    void setAggregateRootId(TAggregateRootId aggregateRootId);
}
