package com.enodeframework.infrastructure;

public abstract class SequenceMessage<TAggregateRootId> extends Message implements ISequenceMessage {

    private TAggregateRootId aggregateRootId;
    private String aggregateRootStringId;
    private String aggregateRootTypeName;
    private int version;

    public TAggregateRootId getAggregateRootId() {
        return aggregateRootId;
    }

    public void setAggregateRootId(TAggregateRootId aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
        this.aggregateRootStringId = aggregateRootId.toString();
    }

    @Override
    public String getAggregateRootStringId() {
        return aggregateRootStringId;
    }

    @Override
    public void setAggregateRootStringId(String aggregateRootStringId) {
        this.aggregateRootStringId = aggregateRootStringId;
    }

    @Override
    public String getAggregateRootTypeName() {
        return aggregateRootTypeName;
    }

    @Override
    public void setAggregateRootTypeName(String aggregateRootTypeName) {
        this.aggregateRootTypeName = aggregateRootTypeName;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String getRoutingKey() {
        return aggregateRootStringId;
    }
}
