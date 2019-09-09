package org.enodeframework.eventing;

public interface IProcessingDomainEventStreamMessageProcessor {
    /**
     * Process the given message.
     *
     * @param processingMessage
     */
    void process(ProcessingDomainEventStreamMessage processingMessage);

    void start();

    void stop();
}
