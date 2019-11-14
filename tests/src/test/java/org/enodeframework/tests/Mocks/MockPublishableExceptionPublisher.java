package org.enodeframework.tests.Mocks;

import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.messaging.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

public class MockPublishableExceptionPublisher implements IMessagePublisher<IDomainException> {
    private static CompletableFuture<Void> _successResultTask = CompletableFuture.completedFuture(null);
    private int _expectFailedCount = 0;
    private int _currentFailedCount = 0;
    private FailedType _failedType;

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
    public CompletableFuture<Void> publishAsync(IDomainException message) {
        if (_currentFailedCount < _expectFailedCount) {
            _currentFailedCount++;
            if (_failedType == FailedType.UnKnownException) {
                throw new ENodeRuntimeException("PublishPublishableExceptionAsyncUnKnownException" + _currentFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new IORuntimeException("PublishPublishableExceptionAsyncIOException" + _currentFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
                throw new ENodeRuntimeException("PublishPublishableExceptionAsyncUnKnownException" + _currentFailedCount);
            }
        }
        return _successResultTask;
    }
}
