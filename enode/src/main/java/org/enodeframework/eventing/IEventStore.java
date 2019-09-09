package org.enodeframework.eventing;

import org.enodeframework.common.io.AsyncTaskResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IEventStore {

    /// <summary>Batch append the given event streams to the event store async.
    /// </summary>
    CompletableFuture<AsyncTaskResult<EventAppendResult>> batchAppendAsync(List<DomainEventStream> eventStreams);

    /// <summary>Find a single event stream by aggregateRootId and version async.
    /// </summary>
    /// <returns></returns>
    CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, int version);

    /// <summary>Find a single event stream by aggregateRootId and commandId async.
    /// </summary>
    /// <returns></returns>
    CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, String commandId);

    /**
     * Query a range of event streams of a single aggregate from event store async.
     */
    CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> queryAggregateEventsAsync(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion);
}
