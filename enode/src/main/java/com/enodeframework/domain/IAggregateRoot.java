package com.enodeframework.domain;

import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.IDomainEvent;

import java.util.List;

/**
 * Represents an aggregate root.
 */
public interface IAggregateRoot {
    String uniqueId();

    int version();

    List<IDomainEvent> getChanges();

    /**
     * Accept changes of the aggregate root.
     */
    void acceptChanges();

    /**
     * Replay the given event streams.
     *
     * @param eventStreams
     */
    void replayEvents(List<DomainEventStream> eventStreams);
}
