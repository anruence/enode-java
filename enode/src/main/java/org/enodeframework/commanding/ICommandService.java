package org.enodeframework.commanding;

import java.util.concurrent.CompletableFuture;

/**
 * Represents a command service.
 */
public interface ICommandService {
    /**
     * Send a command asynchronously.
     *
     * @param command The command to send.
     * @return A task which contains the send result of the command.
     */
    CompletableFuture sendAsync(ICommand command);

    /**
     * Execute a command asynchronously with the default command return type.
     *
     * @param command The command to execute.
     * @return A task which contains the result of the command.
     */
    CompletableFuture<CommandResult> executeAsync(ICommand command);

    /**
     * Execute a command asynchronously with the specified command return type.
     *
     * @param command           The command to execute.
     * @param commandReturnType The return type of the command.
     * @return A task which contains the result of the command.
     */
    CompletableFuture<CommandResult> executeAsync(ICommand command, CommandReturnType commandReturnType);
}
