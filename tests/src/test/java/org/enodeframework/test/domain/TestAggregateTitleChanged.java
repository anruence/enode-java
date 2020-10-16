package org.enodeframework.test.domain;

import org.enodeframework.eventing.DomainEvent;

public class TestAggregateTitleChanged extends DomainEvent<String> {
    private String title;

    public TestAggregateTitleChanged() {
    }

    public TestAggregateTitleChanged(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
