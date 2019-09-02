package com.enodeframework.tests.Mocks;

import com.enodeframework.applicationmessage.IApplicationMessage;
import com.enodeframework.common.exception.ENodeRuntimeException;
import com.enodeframework.common.exception.IORuntimeException;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.messaging.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

public class MockApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {
    private static CompletableFuture<AsyncTaskResult> _successResultTask = CompletableFuture.completedFuture(AsyncTaskResult.Success);
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
    public CompletableFuture<AsyncTaskResult> publishAsync(IApplicationMessage message) {
        if (_currentFailedCount < _expectFailedCount) {
            _currentFailedCount++;
            if (_failedType == FailedType.UnKnownException) {
                throw new ENodeRuntimeException("PublishApplicationMessageAsyncUnKnownException" + _currentFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new IORuntimeException("PublishApplicationMessageAsyncIOException" + _currentFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
                return CompletableFuture.completedFuture(new AsyncTaskResult(AsyncTaskStatus.Failed, "PublishApplicationMessageAsyncError" + _currentFailedCount));
            }
        }
        return _successResultTask;
    }
}
