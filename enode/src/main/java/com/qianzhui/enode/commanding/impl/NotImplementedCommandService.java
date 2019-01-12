package com.qianzhui.enode.commanding.impl;

import com.qianzhui.enode.commanding.CommandResult;
import com.qianzhui.enode.commanding.CommandReturnType;
import com.qianzhui.enode.commanding.ICommand;
import com.qianzhui.enode.commanding.ICommandService;
import com.qianzhui.enode.common.io.AsyncTaskResult;

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
