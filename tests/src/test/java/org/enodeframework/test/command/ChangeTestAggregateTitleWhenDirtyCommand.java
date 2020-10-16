package org.enodeframework.test.command;

import org.enodeframework.commanding.Command;

public class ChangeTestAggregateTitleWhenDirtyCommand extends Command<String> {
    private String title;

    private boolean firstExecute;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isFirstExecute() {
        return firstExecute;
    }

    public void setFirstExecute(boolean firstExecute) {
        this.firstExecute = firstExecute;
    }
}