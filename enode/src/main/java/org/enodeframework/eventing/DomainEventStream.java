package org.enodeframework.eventing;

import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.utilities.ObjectId;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DomainEventStream {
    private String id;
    private String commandId;
    private String aggregateRootTypeName;
    private String aggregateRootId;
    private int version;
    private List<IDomainEvent> events;
    private Date timestamp;
    private Map<String, String> items;

    public DomainEventStream(String commandId, String aggregateRootId, String aggregateRootTypeName, int version, Date timestamp, List<IDomainEvent> events, Map<String, String> items) {
        this.id = ObjectId.generateNewStringId();
        this.commandId = commandId;
        this.aggregateRootId = aggregateRootId;
        this.aggregateRootTypeName = aggregateRootTypeName;
        this.version = version;
        this.timestamp = timestamp;
        this.events = events;
        this.items = items == null ? new HashMap<>() : items;
        int sequence = 1;
        for (IDomainEvent event : events) {
            if (event.getVersion() != this.getVersion()) {
                throw new ENodeRuntimeException(String.format("Invalid domain event version, aggregateRootTypeName: %s aggregateRootId: %s expected version: %d, but was: %d",
                        this.getAggregateRootTypeName(),
                        this.getAggregateRootId(),
                        this.getVersion(),
                        event.getVersion()));
            }
            event.setAggregateRootTypeName(aggregateRootTypeName);
            event.setSequence(sequence++);
        }
    }

    public List<IDomainEvent> getEvents() {
        return events;
    }

    public void setEvents(List<IDomainEvent> events) {
        this.events = events;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getAggregateRootTypeName() {
        return aggregateRootTypeName;
    }

    public void setAggregateRootTypeName(String aggregateRootTypeName) {
        this.aggregateRootTypeName = aggregateRootTypeName;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public void setAggregateRootId(String aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<IDomainEvent> events() {
        return events;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getItems() {
        return items;
    }

    public void setItems(Map<String, String> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        String format = "[Id=%s,CommandId=%s,AggregateRootTypeName=%s,AggregateRootId=%s,Version=%d,Timestamp=%tc,Events=%s,Items=%s]";
        return String.format(format,
                id,
                commandId,
                aggregateRootTypeName,
                aggregateRootId,
                version,
                timestamp,
                events.stream().map(x -> x.getClass().getSimpleName()).collect(Collectors.joining("|")),
                items.entrySet().stream().map(x -> x.getKey() + ":" + x.getValue()).collect(Collectors.joining("|")));
    }
}
