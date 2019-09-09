package org.enodeframework.mysql;

import java.util.Date;

/**
 * @author anruence@gmail.com
 */
public class StreamRecord {

    public String Sequence;
    public String AggregateRootTypeName;
    public String AggregateRootId;
    public int Version;
    public String CommandId;
    public Date CreatedOn;
    public String Events;

    public StreamRecord() {

    }

    public StreamRecord(String commandId, String aggregateRootId, String aggregateRootTypeName, int version, Date createdOn, String events) {
        this.AggregateRootTypeName = aggregateRootTypeName;
        this.AggregateRootId = aggregateRootId;
        this.Version = version;
        this.CommandId = commandId;
        this.CreatedOn = createdOn;
        this.Events = events;
    }
}
