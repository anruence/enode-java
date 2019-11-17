package org.enodeframework.domain;

/**
 * Defines a factory to create empty aggregate root.
 */
public interface IAggregateRootFactory {
    /**
     * Create an empty aggregate root with the given type.
     */
    <T extends IAggregateRoot> T createAggregateRoot(Class<T> aggregateRootType);
}
