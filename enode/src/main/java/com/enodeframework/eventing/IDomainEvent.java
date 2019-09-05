package com.enodeframework.eventing;

import com.enodeframework.messaging.IMessage;

public interface IDomainEvent<TAggregateRootId> extends IMessage {
    TAggregateRootId getAggregateRootId();

    void setAggregateRootId(TAggregateRootId aggregateRootId);

    String getAggregateRootStringId();

    void setAggregateRootStringId(String aggregateRootStringId);

    String getAggregateRootTypeName();

    void setAggregateRootTypeName(String aggregateRootTypeName);

    int getVersion();

    void setVersion(int version);

    int getSequence();

    void setSequence(int sequence);
}
