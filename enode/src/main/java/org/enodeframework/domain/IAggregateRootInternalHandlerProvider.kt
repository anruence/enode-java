package org.enodeframework.domain

import org.enodeframework.common.function.Action2
import org.enodeframework.eventing.IDomainEvent
import java.util.*

/**
 * Defines a provider interface to provide the aggregate root internal handler.
 */
interface IAggregateRootInternalHandlerProvider {
    /**
     * Get the internal event handler within the aggregate.
     */
    fun getInternalEventHandler(aggregateRootType: Class<out IAggregateRoot>, eventType: Class<out IDomainEvent<*>>): Action2<IAggregateRoot, IDomainEvent<*>>
}