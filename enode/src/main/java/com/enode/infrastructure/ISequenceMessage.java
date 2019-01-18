package com.enode.infrastructure;

public interface ISequenceMessage extends IMessage {
    String aggregateRootStringId();

    void setAggregateRootStringId(String aggregateRootStringId);

    String aggregateRootTypeName();

    void setAggregateRootTypeName(String aggregateRootTypeName);

    int version();

    void setVersion(int version);
}
