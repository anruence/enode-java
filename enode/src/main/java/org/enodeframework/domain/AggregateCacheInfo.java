package org.enodeframework.domain;

import org.enodeframework.common.extensions.SystemClock;

import java.util.Date;

/**
 * @author anruence@gmail.com
 */
public class AggregateCacheInfo {
    private IAggregateRoot aggregateRoot;
    private Date lastUpdateTime;

    public AggregateCacheInfo(IAggregateRoot aggregateRoot) {
        this.aggregateRoot = aggregateRoot;
        this.lastUpdateTime = new Date();
    }

    public void updateAggregateRoot(IAggregateRoot aggregateRoot) {
        this.aggregateRoot = aggregateRoot;
        this.lastUpdateTime = new Date();
    }

    public boolean isExpired(int timeoutSeconds) {
        return ((SystemClock.now() - lastUpdateTime.getTime())) / 1000 >= timeoutSeconds;
    }

    public IAggregateRoot getAggregateRoot() {
        return aggregateRoot;
    }

    public void setAggregateRoot(IAggregateRoot aggregateRoot) {
        this.aggregateRoot = aggregateRoot;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
