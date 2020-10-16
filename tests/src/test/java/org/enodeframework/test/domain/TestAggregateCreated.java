package org.enodeframework.test.domain;

import org.enodeframework.eventing.DomainEvent;


public class TestAggregateCreated extends DomainEvent<String> {
    private String title;

    public TestAggregateCreated() {
    }

    public TestAggregateCreated(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
