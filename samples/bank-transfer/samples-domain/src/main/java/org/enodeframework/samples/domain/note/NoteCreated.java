package org.enodeframework.samples.domain.note;

import org.enodeframework.eventing.DomainEvent;

public class NoteCreated extends DomainEvent<String> {

    private String title;

    public NoteCreated() {

    }

    public NoteCreated(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
