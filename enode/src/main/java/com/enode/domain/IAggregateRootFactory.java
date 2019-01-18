package com.enode.domain;

/**
 * Defines a factory to create empty aggregate root.
 */
public interface IAggregateRootFactory {

    /**
     * Create an empty aggregate root with the given type.
     *
     * @param aggregateRootType
     * @param <T>
     * @return
     */
    <T extends IAggregateRoot> T createAggregateRoot(Class<T> aggregateRootType);
}
