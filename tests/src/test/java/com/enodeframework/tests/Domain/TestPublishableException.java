package com.enodeframework.tests.Domain;

import com.enodeframework.infrastructure.PublishableException;

import java.util.Map;

public class TestPublishableException extends PublishableException {
    public String AggregateRootId;

    public TestPublishableException(String aggregateRootId) {
    }

    public TestPublishableException() {
    }

    @Override
    public void serializeTo(Map<String, String> serializableInfo) {
        serializableInfo.put("AggregateRootId", AggregateRootId);
    }

    @Override
    public void restoreFrom(Map<String, String> serializableInfo) {
        AggregateRootId = serializableInfo.get("AggregateRootId");
    }
}
