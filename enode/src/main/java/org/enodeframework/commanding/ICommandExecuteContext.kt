package org.enodeframework.commanding

/**
 * Represents a context environment for command executor executing command.
 */
interface ICommandExecuteContext : ICommandContext, ITrackingContext {
    /**
     * Notify the given command is executed.
     */
    fun onCommandExecutedAsync(commandResult: CommandResult?)
}