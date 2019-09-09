package org.enodeframework.domain;

import org.enodeframework.infrastructure.IObjectProxy;

import java.util.concurrent.CompletableFuture;

public interface IAggregateRepositoryProxy extends IObjectProxy {
    CompletableFuture<IAggregateRoot> getAsync(String aggregateRootId);
}
