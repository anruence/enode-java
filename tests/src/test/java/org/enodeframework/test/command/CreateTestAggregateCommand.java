package org.enodeframework.test.command;

import org.enodeframework.commanding.Command;

public class CreateTestAggregateCommand extends Command<String> {
    public String Title;
    public int SleepMilliseconds;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public int getSleepMilliseconds() {
        return SleepMilliseconds;
    }

    public void setSleepMilliseconds(int sleepMilliseconds) {
        SleepMilliseconds = sleepMilliseconds;
    }
}
