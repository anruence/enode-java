package com.enode.infrastructure;

import com.enode.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface IPublishedVersionStore {

    CompletableFuture<AsyncTaskResult> updatePublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId, int publishedVersion);

    CompletableFuture<AsyncTaskResult<Integer>> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId);
}
