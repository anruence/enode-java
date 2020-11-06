package org.enodeframework.commanding

import org.enodeframework.domain.IAggregateRoot

interface ITrackingContext {
    /**
     * Get all the tracked aggregates.
     */
    val trackedAggregateRoots: List<IAggregateRoot>

    /**
     * Clear the tracking context.
     */
    fun clear()
}