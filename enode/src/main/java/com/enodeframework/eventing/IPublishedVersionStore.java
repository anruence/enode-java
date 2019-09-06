package com.enodeframework.eventing;

import com.enodeframework.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public interface IPublishedVersionStore {
    /**
     * Update the published version for the given aggregate.
     *
     * @param processorName
     * @param aggregateRootTypeName
     * @param aggregateRootId
     * @param publishedVersion
     * @return
     */
    CompletableFuture<AsyncTaskResult> updatePublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId, int publishedVersion);

    /**
     * Get the current published version for the given aggregate.
     *
     * @param processorName
     * @param aggregateRootTypeName
     * @param aggregateRootId
     * @return
     */
    CompletableFuture<AsyncTaskResult<Integer>> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId);
}
