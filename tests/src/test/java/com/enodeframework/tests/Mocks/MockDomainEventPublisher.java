package com.enodeframework.tests.Mocks;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.Task;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.infrastructure.IMessagePublisher;
import com.enodeframework.infrastructure.WrappedRuntimeException;

import java.util.concurrent.CompletableFuture;

public class MockDomainEventPublisher implements IMessagePublisher<DomainEventStreamMessage> {

    private int _expectFailedCount = 0;
    private int _currentFailedCount = 0;
    private FailedType _failedType;

    public void Reset() {
        _failedType = FailedType.None;
        _expectFailedCount = 0;
        _currentFailedCount = 0;
    }

    public void setExpectFailedCount(FailedType failedType, int count) {
        _failedType = failedType;
        _expectFailedCount = count;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> publishAsync(DomainEventStreamMessage message) {
        if (_currentFailedCount < _expectFailedCount) {
            _currentFailedCount++;

            if (_failedType == FailedType.UnKnownException) {
                throw new WrappedRuntimeException("PublishDomainEventStreamMessageAsyncUnKnownException" + _currentFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new WrappedRuntimeException("PublishDomainEventStreamMessageAsyncIOException" + _currentFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
                return Task.fromResult(new AsyncTaskResult(AsyncTaskStatus.Failed, "PublishDomainEventStreamMessageAsyncError" + _currentFailedCount));
            }
        }
        return Task.fromResult(AsyncTaskResult.Success);
    }
}
