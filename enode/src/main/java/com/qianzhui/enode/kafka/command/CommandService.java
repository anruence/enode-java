package com.qianzhui.enode.kafka.command;

import com.qianzhui.enode.commanding.CommandResult;
import com.qianzhui.enode.commanding.CommandReturnType;
import com.qianzhui.enode.commanding.ICommand;
import com.qianzhui.enode.commanding.ICommandService;
import com.qianzhui.enode.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public class CommandService implements ICommandService {

    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(ICommand command) {
        return null;
    }

    @Override
    public CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command) {
        return null;
    }

    @Override
    public CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command, CommandReturnType commandReturnType) {
        return null;
    }
}
