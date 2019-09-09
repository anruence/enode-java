package org.enodeframework.commanding;

import org.enodeframework.domain.IAggregateRoot;

import java.util.List;

public interface ITrackingContext {
    /**
     * Get all the tracked aggregates.
     */
    List<IAggregateRoot> getTrackedAggregateRoots();

    /**
     * Clear the tracking context.
     */
    void clear();
}
