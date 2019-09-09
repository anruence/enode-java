package org.enodeframework.tests.CommandHandlers;

import org.enodeframework.applicationmessage.ApplicationMessage;

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
