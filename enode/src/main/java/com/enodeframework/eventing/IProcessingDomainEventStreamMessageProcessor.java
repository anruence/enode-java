package com.enodeframework.eventing;

import com.enodeframework.eventing.ProcessingDomainEventStreamMessage;

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
