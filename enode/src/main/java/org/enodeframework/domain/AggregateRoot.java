package org.enodeframework.domain;

import com.google.common.collect.Lists;
import org.enodeframework.common.container.ObjectContainer;
import org.enodeframework.common.function.Action2;
import org.enodeframework.common.utils.Assert;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.IDomainEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Represents an abstract base aggregate root.
 *
 * @param <TAggregateRootId>
 */
public abstract class AggregateRoot<TAggregateRootId> implements IAggregateRoot {
    /**
     * dynamic inject through ApplicationContext instance
     */
    private final IAggregateRootInternalHandlerProvider aggregateRootInternalHandlerProvider;
    protected TAggregateRootId id;
    protected int version;
    private final List<IDomainEvent<?>> emptyEvents = new ArrayList<>();
    private Queue<IDomainEvent<?>> uncommittedEvents = new ConcurrentLinkedQueue<>();

    protected AggregateRoot() {
        aggregateRootInternalHandlerProvider = ObjectContainer.resolve(IAggregateRootInternalHandlerProvider.class);
    }

    protected AggregateRoot(TAggregateRootId id) {
        this(id, 0);
    }

    protected AggregateRoot(TAggregateRootId id, int version) {
        this();
        Assert.nonNull(id, "id");
        this.id = id;
        if (version < 0) {
            throw new IllegalArgumentException(String.format("Version cannot small than zero, aggregateRootId: %s, version: %d", id, version));
        }
        this.version = version;
    }

    public TAggregateRootId getId() {
        return this.id;
    }

    protected void applyEvent(IDomainEvent<TAggregateRootId> domainEvent) {
        Assert.nonNull(domainEvent, "domainEvent");
        Assert.nonNull(id, "AggregateRootId");
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
        Action2<IAggregateRoot, IDomainEvent<?>> handler = aggregateRootInternalHandlerProvider.getInternalEventHandler(getClass(), (Class<? extends IDomainEvent<?>>) domainEvent.getClass());
        if (this.id == null && domainEvent.getVersion() == 1) {
            this.id = (TAggregateRootId) domainEvent.getAggregateRootId();
        }
        handler.apply(this, domainEvent);
    }

    private void appendUncommittedEvent(IDomainEvent<TAggregateRootId> domainEvent) {
        if (uncommittedEvents == null) {
            uncommittedEvents = new ConcurrentLinkedQueue<>();
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
        Optional.ofNullable(eventStreams).orElse(new ArrayList<>())
            .forEach(eventStream -> {
                verifyEvent(eventStream);
                eventStream.events().forEach(this::handleEvent);
                this.version = eventStream.getVersion();
            });
    }
}
