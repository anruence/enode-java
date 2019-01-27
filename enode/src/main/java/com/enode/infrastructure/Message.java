package com.enode.infrastructure;

import com.enode.common.utilities.ObjectId;

import java.util.Date;

public abstract class Message implements IMessage {
    public String _id;
    public Date _timestamp;
    public int _sequence;

    public Message() {
        _id = ObjectId.generateNewStringId();
        _timestamp = new Date();
        _sequence = 1;
    }

    @Override
    public String id() {
        return _id;
    }

    @Override
    public void setId(String id) {
        _id = id;
    }

    @Override
    public Date timestamp() {
        return _timestamp;
    }

    @Override
    public void setTimestamp(Date timeStamp) {
        _timestamp = timeStamp;
    }

    @Override
    public int sequence() {
        return _sequence;
    }

    @Override
    public void setSequence(int sequence) {
        _sequence = sequence;
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
