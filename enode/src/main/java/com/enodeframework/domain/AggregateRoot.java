package com.enodeframework.domain;

import com.enodeframework.common.container.SpringObjectContainer;
import com.enodeframework.common.function.Action2;
import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.IDomainEvent;
import com.enodeframework.infrastructure.WrappedRuntimeException;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Represents an abstract base aggregate root.
 *
 * @param <TAggregateRootId>
 */
public abstract class AggregateRoot<TAggregateRootId> implements IAggregateRoot {

    /**
     * dynamic inject through ApplicationContext instance
     */
    private static IAggregateRootInternalHandlerProvider eventHandlerProvider;

    protected TAggregateRootId id;
    protected int version;
    private List<IDomainEvent> emptyEvents = new ArrayList<>();
    private Queue<IDomainEvent> uncommittedEvents;

    protected AggregateRoot() {
        uncommittedEvents = new ConcurrentLinkedDeque<IDomainEvent>() {
        };
    }

    protected AggregateRoot(TAggregateRootId id) {
        this();
        if (id == null) {
            throw new IllegalArgumentException("id");
        }
        this.id = id;
    }

    protected AggregateRoot(TAggregateRootId id, int version) {
        this(id);
        if (version < 0) {
            throw new IllegalArgumentException(String.format("Version cannot small than zero, aggregateRootId: %s, version: %d", id, version));
        }
        this.version = version;
    }

    public TAggregateRootId id() {
        return this.id;
    }

    protected void applyEvent(IDomainEvent<TAggregateRootId> domainEvent) {
        if (domainEvent == null) {
            throw new NullPointerException("domainEvent");
        }

        if (id == null) {
            throw new RuntimeException("Aggregate root id cannot be null.");
        }
        domainEvent.setAggregateRootId(id);
        domainEvent.setVersion(version + 1);
        handleEvent(domainEvent);
        appendUncommittedEvent(domainEvent);
    }

    protected void applyEvents(List<IDomainEvent<TAggregateRootId>> domainEvents) {
        for (IDomainEvent<TAggregateRootId> domainEvent : domainEvents) {
            applyEvent(domainEvent);
        }
    }

    private void handleEvent(IDomainEvent domainEvent) {
        if (eventHandlerProvider == null) {
            eventHandlerProvider = SpringObjectContainer.resolveStatic(IAggregateRootInternalHandlerProvider.class);
        }
        Action2<IAggregateRoot, IDomainEvent> handler = eventHandlerProvider.getInternalEventHandler(getClass(), domainEvent.getClass());
        if (handler == null) {
            throw new RuntimeException(String.format("Could not find event handler for [%s] of [%s]", domainEvent.getClass().getName(), getClass().getName()));
        }
        if (this.id == null && domainEvent.version() == 1) {
            this.id = (TAggregateRootId) domainEvent.aggregateRootId();
        }
        try {
            handler.apply(this, domainEvent);
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    private void appendUncommittedEvent(IDomainEvent<TAggregateRootId> domainEvent) {
        if (uncommittedEvents == null) {
            uncommittedEvents = new ConcurrentLinkedDeque<>();
        }
        if (uncommittedEvents.stream().anyMatch(x -> x.getClass().equals(domainEvent.getClass()))) {
            throw new UnsupportedOperationException(String.format("Cannot apply duplicated domain event type: %s, current aggregateRoot type: %s, id: %s", domainEvent.getTypeName(), this.getClass().getName(), id));
        }
        uncommittedEvents.add(domainEvent);
    }

    private void verifyEvent(DomainEventStream eventStream) {
        if (eventStream.version() > 1 && !eventStream.aggregateRootId().equals(this.uniqueId())) {
            throw new UnsupportedOperationException(String.format("Invalid domain event stream, aggregateRootId:%s, expected aggregateRootId:%s, type:%s", eventStream.aggregateRootId(), this.uniqueId(), this.getClass().getName()));
        }
        if (eventStream.version() != this.version() + 1) {
            throw new UnsupportedOperationException(String.format("Invalid domain event stream, version:%d, expected version:%d, current aggregateRoot type:%s, id:%s", eventStream.version(), this.version(), this.getClass().getName(), this.uniqueId()));
        }
    }

    @Override
    public String uniqueId() {
        if (id != null) {
            return id.toString();
        }

        return null;
    }

    @Override
    public int version() {
        return version;
    }

    @Override
    public List<IDomainEvent> getChanges() {
        if (uncommittedEvents == null) {
            return emptyEvents;
        }

        return Lists.newArrayList(uncommittedEvents);
    }

    @Override
    public void acceptChanges(int newVersion) {
        if (version + 1 != newVersion) {
            throw new UnsupportedOperationException(String.format("Cannot accept invalid version: %d, expect version: %d, current aggregateRoot type: %s, id: %s", newVersion, version + 1, this.getClass().getName(), id));
        }
        this.version = newVersion;
        uncommittedEvents.clear();
    }

    @Override
    public void replayEvents(List<DomainEventStream> eventStreams) {
        if (eventStreams == null) {
            return;
        }

        eventStreams.forEach(eventStream -> {
            verifyEvent(eventStream);
            eventStream.events().forEach(this::handleEvent);

            this.version = eventStream.version();
        });
    }
}
