package com.enode.commanding;

import com.enode.domain.IAggregateRoot;

import java.util.List;

public interface ITrackingContext {
    List<IAggregateRoot> getTrackedAggregateRoots();

    void clear();
}
