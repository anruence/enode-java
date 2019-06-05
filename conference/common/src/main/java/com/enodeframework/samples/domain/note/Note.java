package com.enodeframework.samples.domain.note;

import com.enodeframework.domain.AggregateRoot;

public class Note extends AggregateRoot<String> {

    private String _title;

    public Note() {

    }

    public Note(String id, String title) {
        super(id);
        applyEvent(new NoteCreated(title));
    }

    public void changeTitle(String title) {
        applyEvent(new NoteTitleChanged(title));
        applyEvent(new NoteTitleChanged2(title + "2"));
    }

    protected void handle(NoteCreated evnt) {
        _title = evnt.getTitle();
    }

    protected void handle(NoteTitleChanged evnt) {
        _title = evnt.getTitle();
    }

    protected void handle(NoteTitleChanged2 evnt) {
        _title = evnt.getTitle();
    }

}
