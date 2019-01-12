package com.qianzhui.enodesamples.notesample.commands;

import com.qianzhui.enode.commanding.Command;

public class ChangeNoteTitleCommand extends Command<String> {
    private String title;

    public ChangeNoteTitleCommand(String aggregateRootId, String title) {
        super(aggregateRootId);
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
