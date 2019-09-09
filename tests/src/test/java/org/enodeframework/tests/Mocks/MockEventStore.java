package org.enodeframework.tests.Mocks;

import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.AsyncTaskStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.EventAppendResult;
import org.enodeframework.eventing.IEventStore;
import org.enodeframework.eventing.impl.InMemoryEventStore;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MockEventStore implements IEventStore {
    public boolean SupportBatchAppendEvent;
    private int _expectFailedCount = 0;
    private int _currentFailedCount = 0;
    private FailedType _failedType;
    private InMemoryEventStore _inMemoryEventStore = new InMemoryEventStore();

    public MockEventStore() {
        SupportBatchAppendEvent = true;
    }

    public void Reset() {
        _failedType = FailedType.None;
        _expectFailedCount = 0;
        _currentFailedCount = 0;
    }

    public void SetExpectFailedCount(FailedType failedType, int count) {
        _failedType = failedType;
        _expectFailedCount = count;
    }

    @Override
    public CompletableFuture<AsyncTaskResult<EventAppendResult>> batchAppendAsync(List<DomainEventStream> eventStreams) {
        if (_currentFailedCount < _expectFailedCount) {
            _currentFailedCount++;
            if (_failedType == FailedType.UnKnownException) {
                throw new ENodeRuntimeException("BatchAppendAsyncUnKnownException" + _currentFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new IORuntimeException("BatchAppendAsyncIOException" + _currentFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
                return Task.fromResult(new AsyncTaskResult<EventAppendResult>(AsyncTaskStatus.Failed, "BatchAppendAsyncError" + _currentFailedCount));
            }
        }
        return _inMemoryEventStore.batchAppendAsync(eventStreams);
    }

    @Override
    public CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, int version) {
        if (_currentFailedCount < _expectFailedCount) {
            _currentFailedCount++;
            if (_failedType == FailedType.UnKnownException) {
                throw new ENodeRuntimeException("AppendAsyncUnKnownException" + _currentFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new IORuntimeException("AppendAsyncIOException" + _currentFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
                return Task.fromResult(new AsyncTaskResult<DomainEventStream>(AsyncTaskStatus.Failed, "AppendAsyncError" + _currentFailedCount));
            }
        }
        return _inMemoryEventStore.findAsync(aggregateRootId, version);
    }

    @Override
    public CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, String commandId) {
        return _inMemoryEventStore.findAsync(aggregateRootId, commandId);
    }

    @Override
    public CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> queryAggregateEventsAsync(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion) {
        throw new NotImplementedException();
    }
}
