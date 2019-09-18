package org.enodeframework.publishableexception;

import org.enodeframework.common.utilities.ObjectId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class PublishableException extends RuntimeException implements IPublishableException {
    private static final long serialVersionUID = 2099914413380872726L;
    private String id;
    private Date timestamp;
    private Map<String, String> items;

    public PublishableException() {
        id = ObjectId.generateNewStringId();
        timestamp = new Date();
        items = new HashMap<>();
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

    @Override
    public Map<String, String> getItems() {
        return items;
    }

    @Override
    public void setItems(Map<String, String> items) {
        this.items = items;
    }

    @Override
    public void mergeItems(Map<String, String> mitems) {
        if (mitems == null || mitems.size() == 0) {
            return;
        }
        if (this.items == null) {
            this.items = new HashMap<>();
        }
        for (Map.Entry<String, String> entry : mitems.entrySet()) {
            if (!this.items.containsKey(entry.getKey())) {
                this.items.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
