package com.enode.infrastructure;

public abstract class SequenceMessage<TAggregateRootId> extends Message implements ISequenceMessage {

    private TAggregateRootId _aggregateRootId;
    private String _aggregateRootStringId;
    private String _aggregateRootTypeName;
    private int _version;

    public TAggregateRootId aggregateRootId() {
        return _aggregateRootId;
    }

    public void setAggregateRootId(TAggregateRootId aggregateRootId) {
        _aggregateRootId = aggregateRootId;
        _aggregateRootStringId = aggregateRootId.toString();
    }

    @Override
    public String aggregateRootStringId() {
        return _aggregateRootStringId;
    }

    @Override
    public void setAggregateRootStringId(String aggregateRootStringId) {
        this._aggregateRootStringId = aggregateRootStringId;
    }

    @Override
    public String aggregateRootTypeName() {
        return _aggregateRootTypeName;
    }

    @Override
    public void setAggregateRootTypeName(String aggregateRootTypeName) {
        _aggregateRootTypeName = aggregateRootTypeName;
    }

    @Override
    public int version() {
        return _version;
    }

    @Override
    public void setVersion(int version) {
        _version = version;
    }

    @Override
    public String getRoutingKey() {
        return _aggregateRootStringId;
    }
}
