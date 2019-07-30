package com.enodeframework.eventing.impl;

import java.util.Date;

/**
 * @author anruence@gmail.com
 */
public class StreamRecord {
    private Long Sequence;
    private String AggregateRootTypeName;
    private String AggregateRootId;
    private int Version;
    private String CommandId;
    private Date CreatedOn;
    private String Events;

    public StreamRecord() {
    }

    public StreamRecord(String commandId, String aggregateRootId, String aggregateRootTypeName, int version,
                        Date createdOn, String events) {
        this.AggregateRootTypeName = aggregateRootTypeName;
        this.AggregateRootId = aggregateRootId;
        this.Version = version;
        this.CommandId = commandId;
        this.CreatedOn = createdOn;
        this.Events = events;
    }

    public String getAggregateRootTypeName() {
        return AggregateRootTypeName;
    }

    public void setAggregateRootTypeName(String aggregateRootTypeName) {
        this.AggregateRootTypeName = aggregateRootTypeName;
    }

    public String getAggregateRootId() {
        return AggregateRootId;
    }

    public void setAggregateRootId(String aggregateRootId) {
        this.AggregateRootId = aggregateRootId;
    }

    public int getVersion() {
        return Version;
    }

    public void setVersion(int version) {
        this.Version = version;
    }

    public String getCommandId() {
        return CommandId;
    }

    public void setCommandId(String commandId) {
        this.CommandId = commandId;
    }

    public Date getCreatedOn() {
        return CreatedOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.CreatedOn = createdOn;
    }

    public String getEvents() {
        return Events;
    }

    public void setEvents(String events) {
        this.Events = events;
    }

    public Long getSequence() {
        return Sequence;
    }

    public StreamRecord setSequence(Long sequence) {
        this.Sequence = sequence;
        return this;
    }
}
