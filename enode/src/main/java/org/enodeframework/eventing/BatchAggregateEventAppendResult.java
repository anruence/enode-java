package org.enodeframework.eventing;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class BatchAggregateEventAppendResult {
    private final int expectedAggregateRootCount;
    public ConcurrentHashMap<String, AggregateEventAppendResult> aggregateEventAppendResultDict = new ConcurrentHashMap<>();
    public CompletableFuture<EventAppendResult> taskCompletionSource = new CompletableFuture<>();

    public BatchAggregateEventAppendResult(int expectedAggregateRootCount) {
        this.expectedAggregateRootCount = expectedAggregateRootCount;
    }

    public void addCompleteAggregate(String aggregateRootId, AggregateEventAppendResult result) {
        if (aggregateEventAppendResultDict.putIfAbsent(aggregateRootId, result) == null) {
            int completedAggregateRootCount = aggregateEventAppendResultDict.keySet().size();
            if (completedAggregateRootCount == expectedAggregateRootCount) {
                EventAppendResult eventAppendResult = new EventAppendResult();
                for (Map.Entry<String, AggregateEventAppendResult> entry : aggregateEventAppendResultDict.entrySet()) {
                    if (entry.getValue().getEventAppendStatus() == EventAppendStatus.Success) {
                        eventAppendResult.addSuccessAggregateRootId(entry.getKey());
                    } else if (entry.getValue().getEventAppendStatus() == EventAppendStatus.DuplicateEvent) {
                        eventAppendResult.addDuplicateEventAggregateRootId(entry.getKey());
                    } else if (entry.getValue().getEventAppendStatus() == EventAppendStatus.DuplicateCommand) {
                        eventAppendResult.addDuplicateCommandIds(entry.getKey(), entry.getValue().getDuplicateCommandIds());
                    }
                }
                taskCompletionSource.complete(eventAppendResult);
            }
        }
    }
}