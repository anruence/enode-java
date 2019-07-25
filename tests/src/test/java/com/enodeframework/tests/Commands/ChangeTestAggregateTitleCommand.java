package com.enodeframework.tests.Commands;

import com.enodeframework.commanding.Command;

public class ChangeTestAggregateTitleCommand extends Command {
    public String Title;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
}
