package com.qianzhui.enodesamples.notesample.domain;

import com.qianzhui.enode.eventing.DomainEvent;

public class NoteTitleChanged extends DomainEvent<String> {
    private String title;

    public NoteTitleChanged(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
