package org.enodeframework.samples.domain.note;

import org.enodeframework.eventing.DomainEvent;

public class NoteTitleChanged2 extends DomainEvent<String> {
    private String title;

    public NoteTitleChanged2(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
