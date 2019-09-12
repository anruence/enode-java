package org.enodeframework.mysql;

import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.AsyncTaskStatus;
import org.enodeframework.eventing.EventAppendResult;
import org.enodeframework.eventing.EventAppendStatus;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class BatchAggregateEventAppendResult {
    public ConcurrentHashMap<String, AggregateEventAppendResult> aggregateEventAppendResultDict = new ConcurrentHashMap<>();
    public CompletableFuture<AsyncTaskResult<EventAppendResult>> taskCompletionSource = new CompletableFuture<>();
    private final int expectedAggregateRootCount;

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
                        eventAppendResult.addDuplicateCommandId(entry.getValue().getDuplicateCommandId());
                    }
                }
                taskCompletionSource.complete(new AsyncTaskResult<>(AsyncTaskStatus.Success, eventAppendResult));
            }
        }
    }
}

class AggregateEventAppendResult {

    private EventAppendStatus eventAppendStatus;

    private String duplicateCommandId;

    public EventAppendStatus getEventAppendStatus() {
        return eventAppendStatus;
    }

    public void setEventAppendStatus(EventAppendStatus eventAppendStatus) {
        this.eventAppendStatus = eventAppendStatus;
    }

    public String getDuplicateCommandId() {
        return duplicateCommandId;
    }

    public void setDuplicateCommandId(String duplicateCommandId) {
        this.duplicateCommandId = duplicateCommandId;
    }
}