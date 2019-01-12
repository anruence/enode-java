package com.qianzhui.enode.domain;

import com.qianzhui.enode.eventing.DomainEventStream;
import com.qianzhui.enode.eventing.IDomainEvent;

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
