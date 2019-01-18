package com.enode.eventing;

import com.enode.common.io.AsyncTaskResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IEventStore {
    boolean isSupportBatchAppendEvent();

    CompletableFuture<AsyncTaskResult<EventAppendResult>> batchAppendAsync(List<DomainEventStream> eventStreams);

    CompletableFuture<AsyncTaskResult<EventAppendResult>> appendAsync(DomainEventStream eventStream);

    CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, int version);

    CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, String commandId);

    CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> queryAggregateEventsAsync(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion);
}
