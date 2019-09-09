package org.enodeframework.commanding;

public interface ICommandProcessor {
    /**
     * Process the given command.
     *
     * @param processingCommand
     */
    void process(ProcessingCommand processingCommand);

    void start();

    void stop();
}
