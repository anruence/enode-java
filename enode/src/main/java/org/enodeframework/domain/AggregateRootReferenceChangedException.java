package org.enodeframework.domain;

public class AggregateRootReferenceChangedException extends RuntimeException {

    private IAggregateRoot aggregateRoot;

    public AggregateRootReferenceChangedException(IAggregateRoot aggregateRoot) {
        super(String.format("Aggregate root [type=%s,id=%s] reference already changed.", aggregateRoot.getClass().getName(), aggregateRoot.getUniqueId()));
        this.aggregateRoot = aggregateRoot;
    }

    public IAggregateRoot getAggregateRoot() {
        return aggregateRoot;
    }
}