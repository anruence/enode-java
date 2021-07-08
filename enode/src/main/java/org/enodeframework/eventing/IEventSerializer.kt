package org.enodeframework.eventing

/**
 * Represents an event.
 */
interface IEventSerializer {
    /**
     * Serialize the given events to map.
     */
    fun serialize(evnts: List<IDomainEvent<*>>): Map<String, String>

    /**
     * Deserialize the given data to events.
     */
    fun deserialize(data: Map<String, String>): List<IDomainEvent<*>>
}