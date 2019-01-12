package com.qianzhui.enodesamples.notesample.domain;

import com.qianzhui.enode.domain.AggregateRoot;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Note extends AggregateRoot<String> {
    private String _title;

    public Note(){

    }

    public Note(String id, String title) {
        super(id);
        applyEvent(new NoteCreated(title));
    }

    public void changeTitle(String title) {
        applyEvent(new NoteTitleChanged(title));
    }

    protected void handle(NoteCreated evnt) {
        _title = evnt.getTitle();
    }

    protected void handle(NoteTitleChanged evnt) {
        _title = evnt.getTitle();
    }

    public MethodHandle getTestHandle(Class eventClass) throws NoSuchMethodException, IllegalAccessException {
        return MethodHandles.lookup().findVirtual(this.getClass(), "handle", MethodType.methodType(void.class, eventClass));
    }

    public static void main(String[] args) throws NoSuchMethodException, IllegalAccessException {
        /*Class clz = Note.class;
        Method[] declaredMethods = clz.getDeclaredMethods();
        declaredMethods[0].setAccessible(true);

        */

        MethodHandle handle = MethodHandles.lookup().findVirtual(Note.class, "handle", MethodType.methodType(void.class, NoteCreated.class));
    }
}
