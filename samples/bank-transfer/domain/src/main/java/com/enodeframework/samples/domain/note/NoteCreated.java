package com.enodeframework.samples.domain.note;

import com.enodeframework.eventing.DomainEvent;

public class NoteCreated extends DomainEvent<String> {
    private String title;

    public NoteCreated(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
