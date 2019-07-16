package org.enodeframework.eventing;

import org.enodeframework.commanding.ProcessingCommand;

public interface IEventCommittingService {
    /**
     * Commit the given aggregate's domain events to the eventstore async and publish the domain events.
     */
    void commitDomainEventAsync(EventCommittingContext eventCommittingContext);

    /**
     * Publish the given domain event stream async.
     */
    void publishDomainEventAsync(ProcessingCommand processingCommand, DomainEventStream eventStream);
}
