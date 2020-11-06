package org.enodeframework.commanding

/**
 * @author anruence@gmail.com
 */
class ProcessingCommand(val message: ICommand, val commandExecuteContext: ICommandExecuteContext, items: Map<String, Any>) {
    val items: Map<String, Any>
    lateinit var mailBox: ProcessingCommandMailbox
    var sequence: Long = 0
    var isDuplicated = false
    fun completeAsync(commandResult: CommandResult?) {
        commandExecuteContext.onCommandExecutedAsync(commandResult)
    }

    init {
        this.items = items
    }
}