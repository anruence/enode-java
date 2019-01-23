package com.enode.queue.publishableexceptions;

import java.util.Date;
import java.util.Map;

public class PublishableExceptionMessage {
    private String uniqueId;
    private String aggregateRootId;
    private String aggregateRootTypeName;
    private String exceptionType;
    private Date timestamp;
    private Map<String, String> serializableInfo;

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public void setAggregateRootId(String aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }

    public String getAggregateRootTypeName() {
        return aggregateRootTypeName;
    }

    public void setAggregateRootTypeName(String aggregateRootTypeName) {
        this.aggregateRootTypeName = aggregateRootTypeName;
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
