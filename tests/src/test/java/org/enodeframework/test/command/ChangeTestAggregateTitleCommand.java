package org.enodeframework.test.command;

import org.enodeframework.commanding.Command;

public class ChangeTestAggregateTitleCommand extends Command<String> {
    public String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
