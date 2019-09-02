package com.enodeframework.commanding;

import com.enodeframework.domain.IAggregateRoot;

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
