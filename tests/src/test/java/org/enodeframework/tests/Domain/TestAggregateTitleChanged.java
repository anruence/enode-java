package org.enodeframework.tests.Domain;

import org.enodeframework.eventing.DomainEvent;

public class TestAggregateTitleChanged extends DomainEvent<String> {
    public String Title;

    public TestAggregateTitleChanged() {
    }

    public TestAggregateTitleChanged(String title) {
        Title = title;
    }
}
