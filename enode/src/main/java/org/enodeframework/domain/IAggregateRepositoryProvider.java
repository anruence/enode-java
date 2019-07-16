package org.enodeframework.domain;

/**
 * Represents a provider to provide the aggregate repository.
 */
public interface IAggregateRepositoryProvider {
    /**
     * Get the aggregateRepository for the given aggregate type.
     */
    IAggregateRepositoryProxy getRepository(Class<? extends IAggregateRoot> aggregateRootType);
}
