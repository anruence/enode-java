package org.enodeframework.eventing;

import java.util.List;
import java.util.Map;

/**
 * Represents an event.
 */
public interface IEventSerializer {
    /**
     * Serialize the given events to map.
     *
     * @param evnts
     * @return
     */
    Map<String, String> serialize(List<IDomainEvent> evnts);

    /**
     * Deserialize the given data to events.
     *
     * @param data
     * @param domainEventType
     * @param <TEvent>
     * @return
     */
    <TEvent extends IDomainEvent> List<TEvent> deserialize(Map<String, String> data, Class<TEvent> domainEventType);
}
