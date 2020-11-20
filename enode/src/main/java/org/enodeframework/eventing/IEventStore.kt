package org.enodeframework.eventing

import java.util.concurrent.CompletableFuture

interface IEventStore {
    /**
     * Batch append the given event streams to the event store async.
     */
    fun batchAppendAsync(eventStreams: List<DomainEventStream>): CompletableFuture<EventAppendResult>

    /**
     * Find a single event stream by aggregateRootId and version async.
     */
    fun findAsync(aggregateRootId: String, version: Int): CompletableFuture<DomainEventStream?>

    /**
     * Find a single event stream by aggregateRootId and commandId async.
     */
    fun findAsync(aggregateRootId: String, commandId: String): CompletableFuture<DomainEventStream?>

    /**
     * Query a range of event streams of a single aggregate from event store async.
     */
    fun queryAggregateEventsAsync(aggregateRootId: String, aggregateRootTypeName: String, minVersion: Int, maxVersion: Int): CompletableFuture<List<DomainEventStream>>
}