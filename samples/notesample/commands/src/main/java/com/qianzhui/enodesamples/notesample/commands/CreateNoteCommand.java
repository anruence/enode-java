package com.qianzhui.enodesamples.notesample.commands;

import com.qianzhui.enode.commanding.Command;

public class CreateNoteCommand extends Command<String> {
    private String title;

    public CreateNoteCommand(String aggregateRootId, String title) {
        super(aggregateRootId);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
