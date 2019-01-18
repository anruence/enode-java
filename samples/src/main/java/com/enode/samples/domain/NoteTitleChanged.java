package com.enode.samples.domain;

import com.enode.eventing.DomainEvent;

public class NoteTitleChanged extends DomainEvent<String> {
    private String title;

    public NoteTitleChanged(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
