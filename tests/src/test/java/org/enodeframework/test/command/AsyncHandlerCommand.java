package org.enodeframework.test.command;

import org.enodeframework.commanding.Command;

public class AsyncHandlerCommand extends Command<String> {
    public boolean shouldGenerateApplicationMessage;
    public boolean shouldThrowException;
    public boolean shouldThrowIOException;

    public boolean isShouldGenerateApplicationMessage() {
        return shouldGenerateApplicationMessage;
    }

    public void setShouldGenerateApplicationMessage(boolean shouldGenerateApplicationMessage) {
        this.shouldGenerateApplicationMessage = shouldGenerateApplicationMessage;
    }

    public boolean isShouldThrowException() {
        return shouldThrowException;
    }

    public void setShouldThrowException(boolean shouldThrowException) {
        this.shouldThrowException = shouldThrowException;
    }

    public boolean isShouldThrowIOException() {
        return shouldThrowIOException;
    }

    public void setShouldThrowIOException(boolean shouldThrowIOException) {
        this.shouldThrowIOException = shouldThrowIOException;
    }
}
