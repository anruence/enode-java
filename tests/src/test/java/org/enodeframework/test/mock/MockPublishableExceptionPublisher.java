package org.enodeframework.test.mock;

import org.enodeframework.common.exception.EnodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.domain.IDomainException;
import org.enodeframework.messaging.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

public class MockPublishableExceptionPublisher implements IMessagePublisher<IDomainException> {
    private static CompletableFuture<Boolean> successResultTask = CompletableFuture.completedFuture(true);
    private int expectFailedCount = 0;
    private int currentFailedCount = 0;
    private FailedType failedType;

    public void Reset() {
        failedType = FailedType.None;
        expectFailedCount = 0;
        currentFailedCount = 0;
    }

    public void SetExpectFailedCount(FailedType failedType, int count) {
        this.failedType = failedType;
        expectFailedCount = count;
    }

    @Override
    public CompletableFuture<Boolean> publishAsync(IDomainException message) {
        if (currentFailedCount < expectFailedCount) {
            currentFailedCount++;
            if (failedType == FailedType.UnKnownException) {
                throw new EnodeRuntimeException("PublishPublishableExceptionAsyncUnKnownException" + currentFailedCount);
            } else if (failedType == FailedType.IOException) {
                throw new IORuntimeException("PublishPublishableExceptionAsyncIOException" + currentFailedCount);
            } else if (failedType == FailedType.TaskIOException) {
                throw new EnodeRuntimeException("PublishPublishableExceptionAsyncUnKnownException" + currentFailedCount);
            }
        }
        return successResultTask;
    }
}
