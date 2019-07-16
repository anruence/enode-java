package org.enodeframework.tests.repository;

import org.enodeframework.domain.IAggregateRepository;
import org.enodeframework.tests.domain.TestAggregate;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class TestAggregateRepository implements IAggregateRepository<TestAggregate> {
    @Override
    public CompletableFuture<TestAggregate> getAsync(String aggregateRootId) {
        return CompletableFuture.completedFuture(null);
    }
}
