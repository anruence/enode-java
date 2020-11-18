package org.enodeframework.samples.domain.note;

import org.enodeframework.domain.AggregateRoot;

public class Note extends AggregateRoot<String> {
    private String title;

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
        title = evnt.getTitle();
    }

    protected void handle(NoteTitleChanged evnt) {
        title = evnt.getTitle();
    }

    protected void handle(NoteTitleChanged2 evnt) {
        title = evnt.getTitle();
    }
}
