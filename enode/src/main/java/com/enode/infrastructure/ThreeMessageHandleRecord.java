package com.enode.infrastructure;

import java.util.Date;

public class ThreeMessageHandleRecord {
    private String messageId1;
    private String messageId2;
    private String messageId3;
    private String message1TypeName;
    private String message2TypeName;
    private String message3TypeName;
    private String handlerTypeName;
    private String aggregateRootTypeName;
    private String aggregateRootId;
    private int version;
    private Date createdOn;

    public String getMessageId1() {
        return messageId1;
    }

    public void setMessageId1(String messageId1) {
        this.messageId1 = messageId1;
    }

    public String getMessageId2() {
        return messageId2;
    }

    public void setMessageId2(String messageId2) {
        this.messageId2 = messageId2;
    }

    public String getMessageId3() {
        return messageId3;
    }

    public void setMessageId3(String messageId3) {
        this.messageId3 = messageId3;
    }

    public String getMessage1TypeName() {
        return message1TypeName;
    }

    public void setMessage1TypeName(String message1TypeName) {
        this.message1TypeName = message1TypeName;
    }

    public String getMessage2TypeName() {
        return message2TypeName;
    }

    public void setMessage2TypeName(String message2TypeName) {
        this.message2TypeName = message2TypeName;
    }

    public String getMessage3TypeName() {
        return message3TypeName;
    }

    public void setMessage3TypeName(String message3TypeName) {
        this.message3TypeName = message3TypeName;
    }

    public String getHandlerTypeName() {
        return handlerTypeName;
    }

    public void setHandlerTypeName(String handlerTypeName) {
        this.handlerTypeName = handlerTypeName;
    }

    public String getAggregateRootTypeName() {
        return aggregateRootTypeName;
    }

    public void setAggregateRootTypeName(String aggregateRootTypeName) {
        this.aggregateRootTypeName = aggregateRootTypeName;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public void setAggregateRootId(String aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }
}
