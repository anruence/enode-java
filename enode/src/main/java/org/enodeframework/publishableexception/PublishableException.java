package org.enodeframework.publishableexception;

import org.enodeframework.common.utilities.ObjectId;

import java.util.Date;
import java.util.Map;

public abstract class PublishableException extends RuntimeException implements IPublishableException {
    private static final long serialVersionUID = 2099914413380872726L;
    private String id;
    private Date timestamp;

    public PublishableException() {
        id = ObjectId.generateNewStringId();
        timestamp = new Date();
    }

    @Override
    public abstract void serializeTo(Map<String, String> serializableInfo);

    @Override
    public abstract void restoreFrom(Map<String, String> serializableInfo);

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
