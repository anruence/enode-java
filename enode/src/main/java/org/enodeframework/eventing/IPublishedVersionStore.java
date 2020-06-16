package org.enodeframework.eventing;

import java.util.concurrent.CompletableFuture;

public interface IPublishedVersionStore {
    /**
     * Update the published version for the given aggregate.
     */
    CompletableFuture<Integer> updatePublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId, int publishedVersion);

    /**
     * Get the current published version for the given aggregate.
     */
    CompletableFuture<Integer> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId);
}
