package com.enode.eventing;

import com.enode.commanding.ProcessingCommand;

public interface IEventService {
    /**
     * Commit the given aggregate's domain events to the eventstore async and publish the domain events.
     *
     * @param context
     */
    void commitDomainEventAsync(EventCommittingContext context);

    /**
     * Publish the given domain event stream async.
     *
     * @param processingCommand
     * @param eventStream
     */
    void publishDomainEventAsync(ProcessingCommand processingCommand, DomainEventStream eventStream);

    /**
     * Start background tasks.
     */
    void start();

    /**
     * Stop background tasks.
     */
    void stop();
}
