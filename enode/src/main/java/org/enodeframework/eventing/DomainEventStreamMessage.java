package org.enodeframework.eventing;

import com.google.common.collect.Lists;
import org.enodeframework.messaging.Message;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DomainEventStreamMessage extends Message {
    public String aggregateRootId;
    public String aggregateRootTypeName;
    public int version;
    private String commandId;
    private List<IDomainEvent> events = Lists.newArrayList();

    public DomainEventStreamMessage() {
    }

    public DomainEventStreamMessage(String commandId, String aggregateRootId, int version, String aggregateRootTypeName, List<IDomainEvent> events, Map<String, String> items) {
        this.commandId = commandId;
        this.aggregateRootId = aggregateRootId;
        this.aggregateRootTypeName = aggregateRootTypeName;
        this.version = version;
        this.events = events;
        this.items = items;
    }

    public String getCommandId() {
        return commandId;
    }

    public void setCommandId(String commandId) {
        this.commandId = commandId;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public void setAggregateRootId(String aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }

    public String getAggregateRootTypeName() {
        return aggregateRootTypeName;
    }

    public void setAggregateRootTypeName(String aggregateRootTypeName) {
        this.aggregateRootTypeName = aggregateRootTypeName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<IDomainEvent> getEvents() {
        return events;
    }

    public void setEvents(List<IDomainEvent> events) {
        this.events = events;
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
