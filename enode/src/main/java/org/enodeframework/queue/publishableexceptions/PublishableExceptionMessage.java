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
    private Map<String, String> serializableInfo;

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

    public Map<String, String> getSerializableInfo() {
        return serializableInfo;
    }

    public void setSerializableInfo(Map<String, String> serializableInfo) {
        this.serializableInfo = serializableInfo;
    }
}
