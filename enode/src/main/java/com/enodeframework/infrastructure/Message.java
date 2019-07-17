package com.enodeframework.infrastructure;

import com.enodeframework.common.utilities.ObjectId;

import java.util.Date;

public abstract class Message implements IMessage {
    private String id;
    private Date timestamp;
    private int sequence;

    public Message() {
        id = ObjectId.generateNewStringId();
        timestamp = new Date();
        sequence = 1;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int getSequence() {
        return sequence;
    }

    @Override
    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public String getRoutingKey() {
        return null;
    }

    @Override
    public String getTypeName() {
        return this.getClass().getName();
    }
}
