package com.enode.commanding;

import java.util.concurrent.CompletableFuture;

public interface ICommandHandler<T extends ICommand> {
    /**
     * Handle the given aggregate command.
     *
     * @param context
     * @param command
     * @return
     */
    CompletableFuture handleAsync(ICommandContext context, T command);
}
