package org.enodeframework.test.domain;

import com.google.common.collect.Lists;
import org.enodeframework.domain.AggregateRoot;

public class TestAggregate extends AggregateRoot<String> {
    private String title;

    public TestAggregate() {
    }

    public TestAggregate(String id, String title) {
        super(id);
        applyEvent(new TestAggregateCreated(title));
    }

    public String getTitle() {
        return title;
    }

    public void changeTitle(String title) {
        applyEvent(new TestAggregateTitleChanged(title));
    }

    public void ThrowException(boolean publishableException) throws Exception {
        if (publishableException) {
            throw new TestPublishableException(id);
        } else {
            throw new Exception("TestException");
        }
    }

    public void testEvents() {
        applyEvents(Lists.newArrayList(new Event1(), new Event2(), new Event3()));
    }

    private void handle(TestAggregateCreated evnt) {
        title = evnt.getTitle();
    }

    private void handle(TestAggregateTitleChanged evnt) {
        title = evnt.getTitle();
    }

    private void handle(Event1 evnt) {
    }

    private void handle(Event2 evnt) {
    }

    private void handle(Event3 evnt) {
    }
}
