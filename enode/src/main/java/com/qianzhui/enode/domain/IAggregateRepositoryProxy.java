package com.qianzhui.enode.domain;

import com.qianzhui.enode.infrastructure.IObjectProxy;

import java.util.concurrent.CompletableFuture;

public interface IAggregateRepositoryProxy extends IObjectProxy {
    CompletableFuture<IAggregateRoot> getAsync(String aggregateRootId);
}
