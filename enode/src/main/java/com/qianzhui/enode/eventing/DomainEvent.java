package com.qianzhui.enode.eventing;

import com.qianzhui.enode.infrastructure.SequenceMessage;

/**
 * Represents an abstract generic domain event.
 */
public abstract class DomainEvent<TAggregateRootId> extends
        SequenceMessage<TAggregateRootId> implements IDomainEvent<TAggregateRootId> {

}
