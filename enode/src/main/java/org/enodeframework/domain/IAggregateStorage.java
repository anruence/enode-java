package org.enodeframework.domain;

import java.util.concurrent.CompletableFuture;

/**
 * Represents an aggregate storage interface.
 */
public interface IAggregateStorage {
    /**
     * Get an aggregate from aggregate storage.
     */
    <T extends IAggregateRoot> CompletableFuture<T> getAsync(Class<T> aggregateRootType, String aggregateRootId);
}
