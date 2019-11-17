package org.enodeframework.eventing;

import java.util.List;
import java.util.Map;

/**
 * Represents an event.
 */
public interface IEventSerializer {
    /**
     * Serialize the given events to map.
     */
    Map<String, String> serialize(List<IDomainEvent> evnts);

    /**
     * Deserialize the given data to events.
     */
    <TEvent extends IDomainEvent> List<TEvent> deserialize(Map<String, String> data, Class<TEvent> domainEventType);
}
