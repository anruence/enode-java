package org.enodeframework.commanding.impl;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandService;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class NotImplementedCommandService implements ICommandService {
    @Override
    public CompletableFuture sendAsync(ICommand command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync(ICommand command) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync(ICommand command, CommandReturnType commandReturnType) {
        throw new UnsupportedOperationException();
    }
}
