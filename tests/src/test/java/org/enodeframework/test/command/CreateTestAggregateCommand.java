package org.enodeframework.test.command;

import org.enodeframework.commanding.Command;

public class CreateTestAggregateCommand extends Command<String> {

    public String title;

    public int sleepMilliseconds;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getSleepMilliseconds() {
        return sleepMilliseconds;
    }

    public void setSleepMilliseconds(int sleepMilliseconds) {
        this.sleepMilliseconds = sleepMilliseconds;
    }
}
