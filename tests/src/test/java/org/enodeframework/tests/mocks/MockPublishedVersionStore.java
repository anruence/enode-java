package org.enodeframework.tests.mocks;

import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.eventing.IPublishedVersionStore;
import org.enodeframework.eventing.impl.InMemoryPublishedVersionStore;

import java.util.concurrent.CompletableFuture;

public class MockPublishedVersionStore implements IPublishedVersionStore {
    private InMemoryPublishedVersionStore _inMemoryPublishedVersionStore = new InMemoryPublishedVersionStore();
    private int _expectGetFailedCount = 0;
    private int _expectUpdateFailedCount = 0;
    private int _currentGetFailedCount = 0;
    private int _currentUpdateFailedCount = 0;
    private FailedType _failedType;

    public void Reset() {
        _failedType = FailedType.None;
        _expectGetFailedCount = 0;
        _expectUpdateFailedCount = 0;
        _currentGetFailedCount = 0;
        _currentUpdateFailedCount = 0;
    }

    public void SetExpectFailedCount(FailedType failedType, int count) {
        _failedType = failedType;
        _expectGetFailedCount = count;
        _expectUpdateFailedCount = count;
    }

    @Override
    public CompletableFuture<Integer> updatePublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId, int publishedVersion) {
        if (_currentUpdateFailedCount < _expectUpdateFailedCount) {
            _currentUpdateFailedCount++;
            if (_failedType == FailedType.UnKnownException) {
                throw new ENodeRuntimeException("UpdatePublishedVersionAsyncUnKnownException" + _currentUpdateFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new IORuntimeException("UpdatePublishedVersionAsyncIOException" + _currentUpdateFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
            }
        }
        return _inMemoryPublishedVersionStore.updatePublishedVersionAsync(processorName, aggregateRootTypeName, aggregateRootId, publishedVersion);
    }

    @Override
    public CompletableFuture<Integer> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId) {
        if (_currentGetFailedCount < _expectGetFailedCount) {
            _currentGetFailedCount++;
            if (_failedType == FailedType.UnKnownException) {
                throw new ENodeRuntimeException("GetPublishedVersionAsyncUnKnownException" + _currentGetFailedCount);
            } else if (_failedType == FailedType.IOException) {
                throw new IORuntimeException("GetPublishedVersionAsyncIOException" + _currentGetFailedCount);
            } else if (_failedType == FailedType.TaskIOException) {
            }
        }
        return _inMemoryPublishedVersionStore.getPublishedVersionAsync(processorName, aggregateRootTypeName, aggregateRootId);
    }
}
