package org.enodeframework.queue.command;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public class CommandTaskCompletionSource {
    private String aggregateRootId;
    private CommandReturnType commandReturnType;
    private CompletableFuture<AsyncTaskResult<CommandResult>> taskCompletionSource;

    public CommandTaskCompletionSource(String aggregateRootId, CommandReturnType commandReturnType, CompletableFuture<AsyncTaskResult<CommandResult>> taskCompletionSource) {
        this.aggregateRootId = aggregateRootId;
        this.commandReturnType = commandReturnType;
        this.taskCompletionSource = taskCompletionSource;
    }

    public CommandReturnType getCommandReturnType() {
        return commandReturnType;
    }

    public void setCommandReturnType(CommandReturnType commandReturnType) {
        this.commandReturnType = commandReturnType;
    }

    public CompletableFuture<AsyncTaskResult<CommandResult>> getTaskCompletionSource() {
        return taskCompletionSource;
    }

    public void setTaskCompletionSource(CompletableFuture<AsyncTaskResult<CommandResult>> taskCompletionSource) {
        this.taskCompletionSource = taskCompletionSource;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public CommandTaskCompletionSource setAggregateRootId(String aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
        return this;
    }
}