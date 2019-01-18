package com.enode.domain;

import com.enode.infrastructure.IObjectProxy;

import java.util.concurrent.CompletableFuture;

public interface IAggregateRepositoryProxy extends IObjectProxy {
    CompletableFuture<IAggregateRoot> getAsync(String aggregateRootId);
}
