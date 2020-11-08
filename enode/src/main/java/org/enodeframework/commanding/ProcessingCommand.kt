package org.enodeframework.commanding

import java.util.concurrent.CompletableFuture

/**
 * @author anruence@gmail.com
 */
class ProcessingCommand(val message: ICommand, val commandExecuteContext: ICommandExecuteContext, items: MutableMap<String, Any>) {
    val items: MutableMap<String, Any>
    lateinit var mailBox: ProcessingCommandMailbox
    var sequence: Long = 0
    var isDuplicated = false
    fun completeAsync(commandResult: CommandResult): CompletableFuture<Boolean> {
        return commandExecuteContext.onCommandExecutedAsync(commandResult)
    }

    init {
        this.items = items
    }
}