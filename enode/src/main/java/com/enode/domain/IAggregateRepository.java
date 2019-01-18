package com.enode.domain;

import java.util.concurrent.CompletableFuture;

public interface IAggregateRepository<TAggregateRoot extends IAggregateRoot> {
    CompletableFuture<TAggregateRoot> getAsync(String aggregateRootId);
}
