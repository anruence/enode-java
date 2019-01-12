package com.qianzhui.enode.commanding;

import com.qianzhui.enode.domain.IAggregateRoot;

import java.util.List;

public interface ITrackingContext {
    List<IAggregateRoot> getTrackedAggregateRoots();

    void clear();
}
