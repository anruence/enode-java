package com.enode.domain;

public class AggregateCacheInfo {
    private IAggregateRoot aggregateRoot;
    private long lastUpdateTimeMillis;

    public AggregateCacheInfo(IAggregateRoot aggregateRoot) {
        this.aggregateRoot = aggregateRoot;
        this.lastUpdateTimeMillis = System.currentTimeMillis();
    }

    public boolean isExpired(int timeoutSeconds) {
        return ((System.currentTimeMillis() - lastUpdateTimeMillis)) / 1000 >= timeoutSeconds;
    }

    public IAggregateRoot getAggregateRoot() {
        return aggregateRoot;
    }

    public void setAggregateRoot(IAggregateRoot aggregateRoot) {
        this.aggregateRoot = aggregateRoot;
    }

    public long getLastUpdateTimeMillis() {
        return lastUpdateTimeMillis;
    }

    public void setLastUpdateTimeMillis(long lastUpdateTimeMillis) {
        this.lastUpdateTimeMillis = lastUpdateTimeMillis;
    }
}
