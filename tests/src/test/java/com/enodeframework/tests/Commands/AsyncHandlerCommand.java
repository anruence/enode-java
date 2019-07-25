package com.enodeframework.tests.Commands;

import com.enodeframework.commanding.Command;

public class AsyncHandlerCommand extends Command {
    public boolean ShouldGenerateApplicationMessage;
    public boolean ShouldThrowException;
    public boolean ShouldThrowIOException;

    public boolean isShouldGenerateApplicationMessage() {
        return ShouldGenerateApplicationMessage;
    }

    public void setShouldGenerateApplicationMessage(boolean shouldGenerateApplicationMessage) {
        ShouldGenerateApplicationMessage = shouldGenerateApplicationMessage;
    }

    public boolean isShouldThrowException() {
        return ShouldThrowException;
    }

    public void setShouldThrowException(boolean shouldThrowException) {
        ShouldThrowException = shouldThrowException;
    }

    public boolean isShouldThrowIOException() {
        return ShouldThrowIOException;
    }

    public void setShouldThrowIOException(boolean shouldThrowIOException) {
        ShouldThrowIOException = shouldThrowIOException;
    }
}
