package com.enodeframework.tests.Commands;

import com.enodeframework.commanding.Command;

public class ChangeMultipleAggregatesCommand extends Command {
    public String AggregateRootId1;
    public String AggregateRootId2;

    public String getAggregateRootId1() {
        return AggregateRootId1;
    }

    public void setAggregateRootId1(String aggregateRootId1) {
        AggregateRootId1 = aggregateRootId1;
    }

    public String getAggregateRootId2() {
        return AggregateRootId2;
    }

    public void setAggregateRootId2(String aggregateRootId2) {
        AggregateRootId2 = aggregateRootId2;
    }
}
