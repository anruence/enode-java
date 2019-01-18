package com.enode.commanding.impl;

import java.util.Date;

public class CommandRecord {
    private String commandId;
    private String aggregateRootId;
    private String messagePayload;
    private String messageTypeName;
    private Date createdOn;

    public CommandRecord() {

    }

    public CommandRecord(String commandId, String aggregateRootId, String messagePayload, String messageTypeName, Date createdOn) {
        this.commandId = commandId;
        this.aggregateRootId = aggregateRootId;
        this.messagePayload = messagePayload;
        this.messageTypeName = messageTypeName;
        this.createdOn = createdOn;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public void setAggregateRootId(String aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }

    public String getMessagePayload() {
        return messagePayload;
    }

    public void setMessagePayload(String messagePayload) {
        this.messagePayload = messagePayload;
    }

    public String getMessageTypeName() {
        return messageTypeName;
    }

    public void setMessageTypeName(String messageTypeName) {
        this.messageTypeName = messageTypeName;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}
