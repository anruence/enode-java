package com.enodeframework.tests.CommandHandlers;

import com.enodeframework.applicationmessage.ApplicationMessage;

public class TestApplicationMessage extends ApplicationMessage {
    public String AggregateRootId;

    public TestApplicationMessage() {
    }

    public TestApplicationMessage(String aggregateRootId) {
        AggregateRootId = aggregateRootId;
    }

    public String GetRoutingKey() {
        return AggregateRootId;
    }
}
