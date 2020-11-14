package org.enodeframework.commanding

interface ICommandProcessor {
    /**
     * Process the given command.
     */
    fun process(processingCommand: ProcessingCommand)

    /**
     * Start processor
     */
    fun start()

    /**
     * Stop processor
     */
    fun stop()
}