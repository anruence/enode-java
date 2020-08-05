package org.enodeframework.tests.domain;

import com.google.common.collect.Lists;
import org.enodeframework.domain.AggregateRoot;

public class TestAggregate extends AggregateRoot<String> {
    private String _title;

    public TestAggregate() {
    }

    public TestAggregate(String id, String title) {
        super(id);
        applyEvent(new TestAggregateCreated(title));
    }

    public String getTitle() {
        return _title;
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

    public void TestEvents() {
        applyEvents(Lists.newArrayList(new Event1(), new Event2(), new Event3()));
    }

    private void Handle(TestAggregateCreated evnt) {
        _title = evnt.Title;
    }

    private void Handle(TestAggregateTitleChanged evnt) {
        _title = evnt.Title;
    }

    private void Handle(Event1 evnt) {
    }

    private void Handle(Event2 evnt) {
    }

    private void Handle(Event3 evnt) {
    }
}
