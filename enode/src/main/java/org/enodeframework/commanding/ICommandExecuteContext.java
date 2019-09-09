package org.enodeframework.commanding;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a context environment for command executor executing command.
 */
public interface ICommandExecuteContext extends ICommandContext, ITrackingContext {
    /**
     * Notify the given command is executed.
     *
     * @param commandResult
     * @return
     */
    CompletableFuture<Void> onCommandExecutedAsync(CommandResult commandResult);
}
