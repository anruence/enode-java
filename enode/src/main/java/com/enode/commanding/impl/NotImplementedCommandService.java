package com.enode.commanding.impl;

import com.enode.commanding.CommandResult;
import com.enode.commanding.CommandReturnType;
import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandService;
import com.enode.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

public class NotImplementedCommandService implements ICommandService {
    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(ICommand command) {
        throw new UnsupportedOperationException();
    }


    @Override
    public CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command, CommandReturnType commandReturnType) {
        throw new UnsupportedOperationException();
    }
}
