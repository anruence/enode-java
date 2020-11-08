package org.enodeframework.test.mock;

import org.enodeframework.common.exception.EnodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.messaging.IMessagePublisher;

import java.util.concurrent.CompletableFuture;

public class MockDomainEventPublisher implements IMessagePublisher<DomainEventStreamMessage> {
    private int expectFailedCount = 0;
    private int currentFailedCount = 0;
    private FailedType failedType;

    public void Reset() {
        failedType = FailedType.None;
        expectFailedCount = 0;
        currentFailedCount = 0;
    }

    public void setExpectFailedCount(FailedType failedType, int count) {
        this.failedType = failedType;
        expectFailedCount = count;
    }

    @Override
    public CompletableFuture<Boolean> publishAsync(DomainEventStreamMessage message) {
        if (currentFailedCount < expectFailedCount) {
            currentFailedCount++;
            if (failedType == FailedType.UnKnownException) {
                throw new EnodeRuntimeException("PublishDomainEventStreamMessageAsyncUnKnownException" + currentFailedCount);
            } else if (failedType == FailedType.IOException) {
                throw new IORuntimeException("PublishDomainEventStreamMessageAsyncIOException" + currentFailedCount);
            } else if (failedType == FailedType.TaskIOException) {
            }
        }
        return null;
    }
}
