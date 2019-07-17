package com.enodeframework.tests.Mocks;

import com.enodeframework.common.exception.EnodeRuntimeException;
import com.enodeframework.common.exception.IORuntimeException;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.Task;
import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.EventAppendResult;
import com.enodeframework.eventing.IEventStore;
import com.enodeframework.eventing.impl.InMemoryEventStore;
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
    public boolean isSupportBatchAppendEvent() {
        return SupportBatchAppendEvent;
    }

    @Override
    public CompletableFuture<AsyncTaskResult<EventAppendResult>> batchAppendAsync(List<DomainEventStream> eventStreams) {
        if (_currentFailedCount < _expectFailedCount) {
            _currentFailedCount++;

            if (_failedType == FailedType.UnKnownException) {
                throw new EnodeRuntimeException("BatchAppendAsyncUnKnownException" + _currentFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new IORuntimeException("BatchAppendAsyncIOException" + _currentFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
                return Task.fromResult(new AsyncTaskResult<EventAppendResult>(AsyncTaskStatus.Failed, "BatchAppendAsyncError" + _currentFailedCount));
            }
        }
        return _inMemoryEventStore.batchAppendAsync(eventStreams);
    }

    @Override
    public CompletableFuture<AsyncTaskResult<EventAppendResult>> appendAsync(DomainEventStream eventStream) {
        if (_currentFailedCount < _expectFailedCount) {
            _currentFailedCount++;

            if (_failedType == FailedType.UnKnownException) {
                throw new EnodeRuntimeException("AppendAsyncUnKnownException" + _currentFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new IORuntimeException("AppendAsyncIOException" + _currentFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
                return Task.fromResult(new AsyncTaskResult<EventAppendResult>(AsyncTaskStatus.Failed, "AppendAsyncError" + _currentFailedCount));
            }
        }
        return _inMemoryEventStore.appendAsync(eventStream);
    }

    @Override
    public CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, int version) {
        if (_currentFailedCount < _expectFailedCount) {
            _currentFailedCount++;

            if (_failedType == FailedType.UnKnownException) {
                throw new EnodeRuntimeException("AppendAsyncUnKnownException" + _currentFailedCount);
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
