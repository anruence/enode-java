package org.enodeframework.samples.commands.note;

import org.enodeframework.commanding.Command;

public class CreateNoteCommand extends Command<String> {
    private String title;

    public CreateNoteCommand() {

    }

    public CreateNoteCommand(String aggregateRootId, String title) {
        super(aggregateRootId);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
