package org.enodeframework.samples.domain.note;

import org.enodeframework.eventing.DomainEvent;

public class NoteTitleChanged extends DomainEvent<String> {
    private String title;

    public NoteTitleChanged() {
    }

    public NoteTitleChanged(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
