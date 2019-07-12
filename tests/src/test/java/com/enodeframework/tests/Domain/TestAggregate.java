package com.enodeframework.tests.Domain;

import com.enodeframework.domain.AggregateRoot;
import com.enodeframework.eventing.DomainEvent;

public class TestAggregate extends AggregateRoot<String> {
    private String _title;

    public TestAggregate(String id, String title) {
        super(id);
        applyEvent(new TestAggregateCreated(title));
    }

    public String getTitle() {
        return _title;
    }

    public void ChangeTitle(String title) {
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
        applyEvents(new DomainEvent[]{new Event1(), new Event2(), new Event3()});
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
