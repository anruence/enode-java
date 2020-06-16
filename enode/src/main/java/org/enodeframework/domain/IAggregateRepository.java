package org.enodeframework.domain;

import java.util.concurrent.CompletableFuture;

public interface IAggregateRepository<T extends IAggregateRoot> {
    CompletableFuture<T> getAsync(String aggregateRootId);
}
