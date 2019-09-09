package org.enodeframework.eventing;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
    private Map<String, String> items = Maps.newHashMap();

    public DomainEventStreamMessage() {
    }

    public DomainEventStreamMessage(String commandId, String aggregateRootId, int version, String aggregateRootTypeName, List<IDomainEvent> events, Map<String, String> items) {
        this.commandId = commandId;
        this.aggregateRootTypeName = aggregateRootTypeName;
        this.aggregateRootId = aggregateRootId;
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

    public Map<String, String> getItems() {
        return items;
    }

    public void setItems(Map<String, String> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return String.format("[MessageId=%s,CommandId=%s,AggregateRootId=%s,AggregateRootTypeName=%s,Version=%d,Events=%s,Items=%s]",
                getId(),
                getCommandId(),
                getAggregateRootId(),
                getAggregateRootTypeName(),
                getVersion(),
                events.stream().map(x -> x.getClass().getName()).collect(Collectors.joining("|")),
                items.entrySet().stream().map(x -> x.getKey() + ":" + x.getValue()).collect(Collectors.joining("|")));
    }

}
