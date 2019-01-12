package com.qianzhui.enodesamples.notesample.domain;

import com.qianzhui.enode.eventing.DomainEvent;

public class NoteCreated extends DomainEvent<String> {
    private String title;

    public NoteCreated(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
