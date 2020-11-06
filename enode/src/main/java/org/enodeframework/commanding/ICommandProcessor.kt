package org.enodeframework.commanding

interface ICommandProcessor {
    /**
     * Process the given command.
     */
    fun process(processingCommand: ProcessingCommand)
    fun start()
    fun stop()
}