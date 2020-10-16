package org.enodeframework.test.domain;

public class InheritTestAggregate extends TestAggregate {
    public InheritTestAggregate() {
    }

    public InheritTestAggregate(String id, String title) {
        super(id, title);
    }

    public void changeMyTitle(String title) {
        applyEvent(new TestAggregateTitleChanged(title));
    }
}
