package org.enodeframework.eventing;

public interface IProcessingEventProcessor {
    /**
     * Process the given processingEvent.
     */
    void process(ProcessingEvent processingEvent);

    /**
     * Start the processor.
     */
    void start();

    /**
     * Stop the processor.
     */
    void stop();
}
