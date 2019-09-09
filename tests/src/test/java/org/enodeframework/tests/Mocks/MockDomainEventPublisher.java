package org.enodeframework.tests.Mocks;

import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.io.AsyncTaskResult;
import org.enodeframework.common.io.AsyncTaskStatus;
import org.enodeframework.common.io.Task;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.messaging.IMessagePublisher;

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
                throw new ENodeRuntimeException("PublishDomainEventStreamMessageAsyncUnKnownException" + _currentFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new IORuntimeException("PublishDomainEventStreamMessageAsyncIOException" + _currentFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
                return Task.fromResult(new AsyncTaskResult(AsyncTaskStatus.Failed, "PublishDomainEventStreamMessageAsyncError" + _currentFailedCount));
            }
        }
        return Task.fromResult(AsyncTaskResult.Success);
    }
}
