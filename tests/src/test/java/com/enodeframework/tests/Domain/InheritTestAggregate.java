package com.enodeframework.tests.Domain;

public class InheritTestAggregate extends TestAggregate {

    public InheritTestAggregate(String id, String title) {
        super(id, title);
    }

    public void ChangeMyTitle(String title) {
        applyEvent(new TestAggregateTitleChanged(title));
    }
}
