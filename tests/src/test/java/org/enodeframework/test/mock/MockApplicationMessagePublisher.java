package org.enodeframework.test.mock;

import org.enodeframework.common.exception.EnodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.common.io.Task;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.messaging.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

public class MockApplicationMessagePublisher implements IMessagePublisher<IApplicationMessage> {
    private static final CompletableFuture<Boolean> successResultTask = Task.completedTask;
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
    public CompletableFuture<Boolean> publishAsync(IApplicationMessage message) {
        if (currentFailedCount < expectFailedCount) {
            currentFailedCount++;
            if (failedType == FailedType.UnKnownException) {
                throw new EnodeRuntimeException("PublishApplicationMessageAsyncUnKnownException" + currentFailedCount);
            } else if (failedType == FailedType.IOException) {
                throw new IORuntimeException("PublishApplicationMessageAsyncIOException" + currentFailedCount);
            } else if (failedType == FailedType.TaskIOException) {
            }
        }
        return successResultTask;
    }
}
