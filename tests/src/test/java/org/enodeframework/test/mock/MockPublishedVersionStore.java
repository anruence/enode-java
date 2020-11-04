package org.enodeframework.test.mock;

import org.enodeframework.common.exception.EnodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.eventing.IPublishedVersionStore;
import org.enodeframework.eventing.impl.InMemoryPublishedVersionStore;

import java.util.concurrent.CompletableFuture;

public class MockPublishedVersionStore implements IPublishedVersionStore {
    private InMemoryPublishedVersionStore _inMemoryPublishedVersionStore = new InMemoryPublishedVersionStore();
    private int _expectGetFailedCount = 0;
    private int _currentGetFailedCount = 0;
    private FailedType _failedType;

    public void Reset() {
        _failedType = FailedType.None;
        _expectGetFailedCount = 0;
        _currentGetFailedCount = 0;
    }

    public void SetExpectFailedCount(FailedType failedType, int count) {
        _failedType = failedType;
        _expectGetFailedCount = count;
    }

    @Override
    public CompletableFuture<Integer> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId) {
        if (_currentGetFailedCount < _expectGetFailedCount) {
            _currentGetFailedCount++;
            if (_failedType == FailedType.UnKnownException) {
                throw new EnodeRuntimeException("GetPublishedVersionAsyncUnKnownException" + _currentGetFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new IORuntimeException("GetPublishedVersionAsyncIOException" + _currentGetFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
            }
        }
        return _inMemoryPublishedVersionStore.getPublishedVersionAsync(processorName, aggregateRootTypeName, aggregateRootId);
    }
}
