package com.enode.eventing.impl;

import java.util.Date;

public class StreamRecord {
    private String aggregateRootTypeName;
    private String aggregateRootId;
    private int version;
    private String commandId;
    private Date createdOn;
    private String events;

    public StreamRecord() {
    }

    public StreamRecord(String commandId, String aggregateRootId, String aggregateRootTypeName, int version,
                        Date createdOn, String events) {
        this.aggregateRootTypeName = aggregateRootTypeName;
        this.aggregateRootId = aggregateRootId;
        this.version = version;
        this.commandId = commandId;
        this.createdOn = createdOn;
        this.events = events;
    }

    public String getAggregateRootTypeName() {
        return aggregateRootTypeName;
    }

    public void setAggregateRootTypeName(String aggregateRootTypeName) {
        this.aggregateRootTypeName = aggregateRootTypeName;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public void setAggregateRootId(String aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getEvents() {
        return events;
    }

    public void setEvents(String events) {
        this.events = events;
    }
}
