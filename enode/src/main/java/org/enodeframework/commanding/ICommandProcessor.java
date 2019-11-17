package org.enodeframework.commanding;

public interface ICommandProcessor {
    /**
     * Process the given command.
     */
    void process(ProcessingCommand processingCommand);

    void start();

    void stop();
}
