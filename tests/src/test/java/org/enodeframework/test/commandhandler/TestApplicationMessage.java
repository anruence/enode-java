package org.enodeframework.test.commandhandler;

import org.enodeframework.messaging.ApplicationMessage;

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
