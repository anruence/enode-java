package com.enodeframework.eventing;

import com.enodeframework.infrastructure.ISequenceMessage;

public interface IDomainEvent<TAggregateRootId> extends ISequenceMessage {
    TAggregateRootId getAggregateRootId();

    void setAggregateRootId(TAggregateRootId aggregateRootId);
}
