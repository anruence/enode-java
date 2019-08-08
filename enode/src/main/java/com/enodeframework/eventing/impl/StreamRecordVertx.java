package com.enodeframework.eventing.impl;

import java.util.Date;

/**
 * @author anruence@gmail.com
 */
public class StreamRecordVertx {
    public Long Sequence;
    public String AggregateRootTypeName;
    public String AggregateRootId;
    public int Version;
    public String CommandId;
    public Date CreatedOn;
    public String Events;

    public StreamRecordVertx() {

    }

    public StreamRecordVertx(String commandId, String aggregateRootId, String aggregateRootTypeName, int version, Date createdOn, String events) {
        this.AggregateRootTypeName = aggregateRootTypeName;
        this.AggregateRootId = aggregateRootId;
        this.Version = version;
        this.CommandId = commandId;
        this.CreatedOn = createdOn;
        this.Events = events;
    }
}
