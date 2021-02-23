package org.enodeframework.test.domain;

import org.enodeframework.domain.DomainException;

import java.util.Map;

public class TestPublishableException extends DomainException {
    public String aggregateRootId;

    public TestPublishableException(String aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }

    public TestPublishableException() {
    }

    @Override
    public void serializeTo(Map<String, Object> serializableInfo) {
        serializableInfo.put("AggregateRootId", aggregateRootId);
    }

    @Override
    public void restoreFrom(Map<String, Object> serializableInfo) {
        aggregateRootId = (String) serializableInfo.get("AggregateRootId");
    }
}
