package com.enodeframework.infrastructure;

public interface ISequenceMessage extends IMessage {
    String getAggregateRootStringId();

    void setAggregateRootStringId(String aggregateRootStringId);

    String getAggregateRootTypeName();

    void setAggregateRootTypeName(String aggregateRootTypeName);

    int getVersion();

    void setVersion(int version);
}
