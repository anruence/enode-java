package com.enodeframework.eventing;

import com.enodeframework.infrastructure.SequenceMessage;

/**
 * Represents an abstract generic domain event.
 */
public abstract class DomainEvent<TAggregateRootId> extends SequenceMessage<TAggregateRootId> implements IDomainEvent<TAggregateRootId> {
}
