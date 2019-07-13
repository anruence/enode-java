package com.enodeframework.infrastructure.impl;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.infrastructure.IPublishedVersionStore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author anruence@gmail.com
 */
public class InMemoryPublishedVersionStore implements IPublishedVersionStore {
    private final CompletableFuture<AsyncTaskResult> successTask = CompletableFuture.completedFuture(AsyncTaskResult.Success);

    private final ConcurrentMap<String, Integer> versionDict = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<AsyncTaskResult> updatePublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId, int publishedVersion) {
        versionDict.put(buildKey(processorName, aggregateRootId), publishedVersion);
        return successTask;
    }

    @Override
    public CompletableFuture<AsyncTaskResult<Integer>> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId) {
        Integer version = versionDict.get(buildKey(processorName, aggregateRootId));
        Integer publishedVersion = version == null ? 0 : version;
        return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Success, publishedVersion));
    }

    private String buildKey(String eventProcessorName, String aggregateRootId) {
        return String.format("%s-%s", eventProcessorName, aggregateRootId);
    }
}
