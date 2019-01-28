package com.enode.samples.commands.note;

import com.enode.commanding.Command;

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
