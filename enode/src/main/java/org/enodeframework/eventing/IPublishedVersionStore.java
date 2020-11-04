package org.enodeframework.eventing;

import java.util.concurrent.CompletableFuture;

public interface IPublishedVersionStore {
    /**
     * Get the current published version for the given aggregate.
     */
    CompletableFuture<Integer> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId);
}
