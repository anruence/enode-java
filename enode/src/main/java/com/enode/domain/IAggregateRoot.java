package com.enode.domain;

import com.enode.eventing.DomainEventStream;
import com.enode.eventing.IDomainEvent;

import java.util.List;

/**
 * Represents an aggregate root.
 */
public interface IAggregateRoot {
    String uniqueId();

    int version();

    List<IDomainEvent> getChanges();

    void acceptChanges(int newVersion);

    void replayEvents(List<DomainEventStream> eventStreams);
}
