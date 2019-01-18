package com.enode.sample.domain;

import com.enode.eventing.DomainEvent;

public class NoteCreated extends DomainEvent<String> {
    private String title;

    public NoteCreated(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
