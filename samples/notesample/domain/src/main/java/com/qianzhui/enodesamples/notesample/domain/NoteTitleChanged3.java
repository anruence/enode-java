package com.qianzhui.enodesamples.notesample.domain;

import com.qianzhui.enode.eventing.DomainEvent;

public class NoteTitleChanged3 extends DomainEvent<String> {
    private String title;

    public NoteTitleChanged3(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
