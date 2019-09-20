package org.enodeframework.eventing;

import org.enodeframework.common.exception.ArgumentException;
import org.enodeframework.common.exception.ENodeRuntimeException;
import org.enodeframework.common.utilities.Linq;
import org.enodeframework.messaging.Message;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DomainEventStream extends Message {
    private String commandId;
    private String aggregateRootTypeName;
    private String aggregateRootId;
    private int version;
    private List<IDomainEvent> events;

    public DomainEventStream(String commandId, String aggregateRootId, String aggregateRootTypeName, Date timestamp, List<IDomainEvent> events, Map<String, String> items) {
        if (events == null || events.size() == 0) {
            throw new ArgumentException("Parameter events cannot be null or empty.");
        }
        this.commandId = commandId;
        this.aggregateRootId = aggregateRootId;
        this.aggregateRootTypeName = aggregateRootTypeName;
        this.version = Linq.first(events).getVersion();
        this.timestamp = timestamp;
        this.events = events;
        this.items = items == null ? new HashMap<>() : items;
        int sequence = 1;
        for (IDomainEvent event : events) {
            if (!aggregateRootId.equals(event.getAggregateRootStringId())) {
                throw new ENodeRuntimeException(String.format("Invalid domain event aggregateRootId, aggregateRootTypeName: %s expected aggregateRootId: %s, but was: %s",
                        aggregateRootTypeName,
                        aggregateRootId,
                        event.getAggregateRootStringId()));
            }

            if (event.getVersion() != this.getVersion()) {
                throw new ENodeRuntimeException(String.format("Invalid domain event version, aggregateRootTypeName: %s aggregateRootId: %s expected version: %d, but was: %d",
                        aggregateRootTypeName,
                        aggregateRootId,
                        version,
                        event.getVersion()));
            }
            event.setCommandId(commandId);
            event.setAggregateRootTypeName(aggregateRootTypeName);
            event.setSequence(sequence++);
            event.setTimestamp(timestamp);
            event.mergeItems(items);
        }
    }

    public List<IDomainEvent> getEvents() {
        return events;
    }

    public void setEvents(List<IDomainEvent> events) {
        this.events = events;
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

    @Override
    public String toString() {
        String format = "[Id=%s,CommandId=%s,AggregateRootId=%s,AggregateRootTypeName=%s,Version=%d,Events=%s,Items=%s,Timestamp=%tc]";
        return String.format(format,
                id,
                commandId,
                aggregateRootId,
                aggregateRootTypeName,
                version,
                events.stream().map(x -> x.getClass().getSimpleName()).collect(Collectors.joining("|")),
                items.entrySet().stream().map(x -> x.getKey() + ":" + x.getValue()).collect(Collectors.joining("|")),
                timestamp
        );
    }
}
