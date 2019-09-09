package org.enodeframework.messaging;

import org.enodeframework.common.utilities.ObjectId;

import java.util.Date;

public abstract class Message implements IMessage {
    private String id;
    private Date timestamp;

    public Message() {
        id = ObjectId.generateNewStringId();
        timestamp = new Date();
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
}
