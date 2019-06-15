package com.enodeframework.samples.domain.note;

import com.enodeframework.eventing.DomainEvent;

public class NoteTitleChanged3 extends DomainEvent<String> {
    private String title;

    public NoteTitleChanged3(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
