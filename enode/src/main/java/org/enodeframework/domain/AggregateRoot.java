package org.enodeframework.domain;

import com.google.common.collect.Lists;
import org.enodeframework.common.container.ObjectContainer;
import org.enodeframework.common.exception.HandlerNotFoundException;
import org.enodeframework.common.function.Action2;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.IDomainEvent;

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
    private static IAggregateRootInternalHandlerProvider aggregateRootInternalHandlerProvider;
    protected TAggregateRootId id;
    protected int version;
    private List<IDomainEvent<?>> emptyEvents = new ArrayList<>();
    private Queue<IDomainEvent<?>> uncommittedEvents;

    protected AggregateRoot() {
        uncommittedEvents = new ConcurrentLinkedDeque<IDomainEvent<?>>() {
        };
    }

    protected AggregateRoot(TAggregateRootId id) {
        this();
        Ensure.notNull(id, "id");
        this.id = id;
    }

    protected AggregateRoot(TAggregateRootId id, int version) {
        this(id);
        if (version < 0) {
            throw new IllegalArgumentException(String.format("Version cannot small than zero, aggregateRootId: %s, version: %d", id, version));
        }
        this.version = version;
    }

    public TAggregateRootId getId() {
        return this.id;
    }

    protected void applyEvent(IDomainEvent<TAggregateRootId> domainEvent) {
        Ensure.notNull(domainEvent, "domainEvent");
        Ensure.notNull(id, "AggregateRootId");
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

    private void handleEvent(IDomainEvent<?> domainEvent) {
        if (aggregateRootInternalHandlerProvider == null) {
            aggregateRootInternalHandlerProvider = ObjectContainer.resolve(IAggregateRootInternalHandlerProvider.class);
        }
        Action2<IAggregateRoot, IDomainEvent<?>> handler = aggregateRootInternalHandlerProvider.getInternalEventHandler(getClass(), domainEvent.getClass());
        if (handler == null) {
            throw new HandlerNotFoundException(String.format("Could not find event handler for [%s] of [%s]", domainEvent.getClass().getName(), getClass().getName()));
        }
        if (this.id == null && domainEvent.getVersion() == 1) {
            this.id = (TAggregateRootId) domainEvent.getAggregateRootId();
        }
        handler.apply(this, domainEvent);
    }

    private void appendUncommittedEvent(IDomainEvent<TAggregateRootId> domainEvent) {
        if (uncommittedEvents == null) {
            uncommittedEvents = new ConcurrentLinkedDeque<>();
        }
        if (uncommittedEvents.stream().anyMatch(x -> x.getClass().equals(domainEvent.getClass()))) {
            throw new UnsupportedOperationException(String.format("Cannot apply duplicated domain event type: %s, current aggregateRoot type: %s, id: %s", domainEvent.getClass(), this.getClass().getName(), id));
        }
        uncommittedEvents.add(domainEvent);
    }

    private void verifyEvent(DomainEventStream eventStream) {
        if (eventStream.getVersion() > 1 && !eventStream.getAggregateRootId().equals(this.getUniqueId())) {
            throw new UnsupportedOperationException(String.format("Invalid domain event stream, aggregateRootId:%s, expected aggregateRootId:%s, type:%s", eventStream.getAggregateRootId(), this.getUniqueId(), this.getClass().getName()));
        }
        if (eventStream.getVersion() != this.getVersion() + 1) {
            throw new UnsupportedOperationException(String.format("Invalid domain event stream, version:%d, expected version:%d, current aggregateRoot type:%s, id:%s", eventStream.getVersion(), this.getVersion(), this.getClass().getName(), this.getUniqueId()));
        }
    }

    @Override
    public String getUniqueId() {
        return id.toString();
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public List<IDomainEvent<?>> getChanges() {
        if (uncommittedEvents == null) {
            return emptyEvents;
        }
        return Lists.newArrayList(uncommittedEvents);
    }

    @Override
    public void acceptChanges() {
        if (uncommittedEvents != null && !uncommittedEvents.isEmpty()) {
            version = uncommittedEvents.peek().getVersion();
            uncommittedEvents.clear();
        }
    }

    @Override
    public void replayEvents(List<DomainEventStream> eventStreams) {
        if (eventStreams == null) {
            return;
        }
        eventStreams.forEach(eventStream -> {
            verifyEvent(eventStream);
            eventStream.events().forEach(this::handleEvent);
            this.version = eventStream.getVersion();
        });
    }
}
