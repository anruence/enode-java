package com.enode.domain.impl;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.IOHelper;
import com.enode.common.logging.ENodeLogger;
import com.enode.common.utilities.CompletableFutureUtil;
import com.enode.domain.IAggregateRoot;
import com.enode.domain.IAggregateRootFactory;
import com.enode.domain.IAggregateSnapshotter;
import com.enode.domain.IAggregateStorage;
import com.enode.eventing.DomainEventStream;
import com.enode.eventing.DomainEventStreamMessage;
import com.enode.eventing.IEventStore;
import com.enode.infrastructure.IMessagePublisher;
import com.enode.infrastructure.IPublishedVersionStore;
import com.enode.infrastructure.ITypeNameProvider;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EventSourcingAggregateStorage implements IAggregateStorage {
    private static final Logger _logger = ENodeLogger.getLog();

    private static final int minVersion = 1;
    private static final int maxVersion = Integer.MAX_VALUE;
    private final IAggregateRootFactory _aggregateRootFactory;
    private final IEventStore _eventStore;
    private final IPublishedVersionStore _publishedVersionStore;
    private final IMessagePublisher<DomainEventStreamMessage> _domainEventPublisher;
    private final IAggregateSnapshotter _aggregateSnapshotter;
    private final ITypeNameProvider _typeNameProvider;
    private final IOHelper _ioHelper;

    @Inject
    public EventSourcingAggregateStorage(
            IAggregateRootFactory aggregateRootFactory,
            IEventStore eventStore,
            IPublishedVersionStore publishedVersionStore,
            IMessagePublisher<DomainEventStreamMessage> domainEventPublisher,
            IAggregateSnapshotter aggregateSnapshotter,
            ITypeNameProvider typeNameProvider,
            IOHelper ioHelper) {
        _aggregateRootFactory = aggregateRootFactory;
        _eventStore = eventStore;
        _publishedVersionStore = publishedVersionStore;
        _domainEventPublisher = domainEventPublisher;
        _aggregateSnapshotter = aggregateSnapshotter;
        _typeNameProvider = typeNameProvider;
        _ioHelper = ioHelper;
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, String aggregateRootId) {
        if (aggregateRootType == null) {
            throw new NullPointerException("aggregateRootType");
        }
        if (aggregateRootId == null) {
            throw new NullPointerException("aggregateRootId");
        }

        CompletableFuture<T> aggregateRootFuture = tryGetFromSnapshot(aggregateRootId, aggregateRootType);
        return aggregateRootFuture.thenApply(aggregateRoot -> {
            if (aggregateRoot != null) {
                return aggregateRoot;
            }
            String aggregateRootTypeName = _typeNameProvider.getTypeName(aggregateRootType);
            CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> eventStreamsFuture = _eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, minVersion, Integer.MAX_VALUE);
            // block to retrieve data
            List<DomainEventStream> eventStreams = CompletableFutureUtil.getValue(eventStreamsFuture).getData();
            T reAggregateRoot = rebuildAggregateRoot(aggregateRootType, eventStreams);
            return reAggregateRoot;
        });
    }

    private <T extends IAggregateRoot> CompletableFuture<T> tryGetFromSnapshot(String aggregateRootId, Class<T> aggregateRootType) {
        CompletableFuture<T> aggregateRootFuture = _aggregateSnapshotter.restoreFromSnapshotAsync(aggregateRootType, aggregateRootId);
        return aggregateRootFuture.thenApply((aggregateRoot) -> {
            if (aggregateRoot == null) {
                return null;
            }
            if (aggregateRoot.getClass() != aggregateRootType || !aggregateRoot.uniqueId().equals(aggregateRootId)) {
                throw new RuntimeException(String.format("AggregateRoot recovery from snapshot is invalid as the aggregateRootType or aggregateRootId is not matched. Snapshot: [aggregateRootType:%s,aggregateRootId:%s], expected: [aggregateRootType:%s,aggregateRootId:%s]",
                        aggregateRoot.getClass(),
                        aggregateRoot.uniqueId(),
                        aggregateRootType,
                        aggregateRootId));
            }
            String aggregateRootTypeName = _typeNameProvider.getTypeName(aggregateRootType);
            CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> eventStreamsFuture = _eventStore.queryAggregateEventsAsync(aggregateRootId, aggregateRootTypeName, aggregateRoot.version() + 1, Integer.MAX_VALUE);
            List<DomainEventStream> eventStreamsAfterSnapshot = CompletableFutureUtil.getValue(eventStreamsFuture).getData();
            aggregateRoot.replayEvents(eventStreamsAfterSnapshot);
            return aggregateRoot;
        });
    }

    private <T extends IAggregateRoot> T rebuildAggregateRoot(Class<T> aggregateRootType, List<DomainEventStream> eventStreams) {
        if (eventStreams == null || eventStreams.isEmpty()) {
            return null;
        }
        T aggregateRoot = _aggregateRootFactory.createAggregateRoot(aggregateRootType);
        aggregateRoot.replayEvents(eventStreams);
        return aggregateRoot;
    }
}
