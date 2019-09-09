package org.enodeframework.domain;

import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.IDomainEvent;

import java.util.List;

/**
 * Represents an aggregate root.
 */
public interface IAggregateRoot {
    /**
     * Represents the unique id of the aggregate root.
     */
    String getUniqueId();

    /**
     * Represents the current version of the aggregate root.
     */
    int getVersion();

    /**
     * Get all the changes of the aggregate root.
     */
    List<IDomainEvent> getChanges();

    /**
     * Accept changes of the aggregate root.
     */
    void acceptChanges();

    /**
     * Replay the given event streams.
     */
    void replayEvents(List<DomainEventStream> eventStreams);
}
