package org.enodeframework.eventing;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IEventStore {

    /**
     * Batch append the given event streams to the event store async.
     */
    CompletableFuture<EventAppendResult> batchAppendAsync(List<DomainEventStream> eventStreams);

    /**
     * Find a single event stream by aggregateRootId and version async.
     */
    CompletableFuture<DomainEventStream> findAsync(String aggregateRootId, int version);

    /**
     * Find a single event stream by aggregateRootId and commandId async.
     */
    CompletableFuture<DomainEventStream> findAsync(String aggregateRootId, String commandId);

    /**
     * parse commandId from exception message with different event store
     */
    default String findDuplicateCommandInException(String errMsg) {
        return "";
    }

    /**
     * Query a range of event streams of a single aggregate from event store async.
     */
    CompletableFuture<List<DomainEventStream>> queryAggregateEventsAsync(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion);
}
