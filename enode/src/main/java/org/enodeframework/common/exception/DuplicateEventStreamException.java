package org.enodeframework.common.exception;

import org.enodeframework.eventing.DomainEventStream;

/**
 * @author anruence@gmail.com
 */
public class DuplicateEventStreamException extends RuntimeException {
    public DuplicateEventStreamException(DomainEventStream domainEventStream) {
        super(String.format("Aggregate root [type=%s,id=%s] event stream already exist in the EventCommittingContextMailBox, eventStreamId: %s",
                domainEventStream.getAggregateRootTypeName(), domainEventStream.getAggregateRootId(), domainEventStream.getId()));
    }
}
