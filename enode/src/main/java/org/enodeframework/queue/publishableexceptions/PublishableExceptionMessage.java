package org.enodeframework.queue.publishableexceptions;

import java.util.Date;
import java.util.Map;

/**
 * @author anruence@gmail.com
 */
public class PublishableExceptionMessage {
    private String uniqueId;
    private String exceptionType;
    private Date timestamp;
    private Map<String, Object> serializableInfo;
    private Map<String, Object> items;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getSerializableInfo() {
        return serializableInfo;
    }

    public void setSerializableInfo(Map<String, Object> serializableInfo) {
        this.serializableInfo = serializableInfo;
    }

    public Map<String, Object> getItems() {
        return items;
    }

    public void setItems(Map<String, Object> items) {
        this.items = items;
    }
}
