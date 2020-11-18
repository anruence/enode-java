package org.enodeframework.domain;

import org.enodeframework.common.utilities.IdGenerator;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public abstract class DomainException extends RuntimeException implements IDomainException {
    private static final long serialVersionUID = 2099914413380872726L;
    private String id;
    private Date timestamp;
    private Map<String, Object> items;

    public DomainException() {
        id = IdGenerator.nextId();
        timestamp = new Date();
        items = new HashMap<>();
    }

    @Override
    public abstract void serializeTo(Map<String, Object> serializableInfo);

    @Override
    public abstract void restoreFrom(Map<String, Object> serializableInfo);

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
    public Map<String, Object> getItems() {
        return items;
    }

    @Override
    public void setItems(Map<String, Object> items) {
        this.items = items;
    }

    @Override
    public void mergeItems(Map<String, Object> mitems) {
        if (mitems == null || mitems.size() == 0) {
            return;
        }
        if (this.items == null) {
            this.items = new HashMap<>();
        }
        for (Map.Entry<String, Object> entry : mitems.entrySet()) {
            if (!this.items.containsKey(entry.getKey())) {
                this.items.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
