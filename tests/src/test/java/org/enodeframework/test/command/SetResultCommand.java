package org.enodeframework.test.command;

import org.enodeframework.commanding.Command;

public class SetResultCommand extends Command {
    public String Result;

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }
}
