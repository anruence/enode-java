package com.qianzhui.enode.eventing;

import com.qianzhui.enode.infrastructure.ISequenceMessage;

public interface IDomainEvent<TAggregateRootId> extends ISequenceMessage {
    TAggregateRootId aggregateRootId ();

    void setAggregateRootId(TAggregateRootId aggregateRootId);
}
