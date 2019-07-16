package org.enodeframework.domain;

import org.enodeframework.infrastructure.IObjectProxy;

import java.util.concurrent.CompletableFuture;

public interface IAggregateRepositoryProxy extends IObjectProxy {
    <T extends IAggregateRoot> CompletableFuture<T> getAsync(String aggregateRootId);
}
