package org.enodeframework.tests.domain;

import org.enodeframework.eventing.DomainEvent;

public class TestAggregateCreated extends DomainEvent<String> {
    public String Title;

    public TestAggregateCreated() {
    }

    public TestAggregateCreated(String title) {
        Title = title;
    }
}
