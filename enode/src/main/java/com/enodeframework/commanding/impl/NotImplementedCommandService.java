package com.enodeframework.commanding.impl;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ICommandService;
import com.enodeframework.common.io.AsyncTaskResult;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
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
