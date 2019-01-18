package com.enode.eventing;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DomainEventStream {
    private String _commandId;
    private String _aggregateRootTypeName;
    private String _aggregateRootId;
    private int _version;
    private List<IDomainEvent> _events;
    private Date _timestamp;
    private Map<String, String> _items;

    public DomainEventStream(String commandId, String aggregateRootId, String aggregateRootTypeName, int version, Date timestamp, List<IDomainEvent> events, Map<String, String> items) {
        _commandId = commandId;
        _aggregateRootId = aggregateRootId;
        _aggregateRootTypeName = aggregateRootTypeName;
        _version = version;
        _timestamp = timestamp;
        _events = events;
        _items = items == null ? new HashMap<>() : items;
        int sequence = 1;

        for (IDomainEvent event : _events) {
            if (event.version() != this.version()) {
                throw new RuntimeException(String.format("Invalid domain event version, aggregateRootTypeName: %s aggregateRootId: %s expected version: %d, but was: %d",
                        this.aggregateRootTypeName(),
                        this.aggregateRootId(),
                        this.version(),
                        event.version()));
            }
            event.setAggregateRootTypeName(aggregateRootTypeName);
            event.setSequence(sequence++);
        }
    }

    public String commandId() {
        return _commandId;
    }

    public void setCommandId(String commandId) {
        _commandId = commandId;
    }

    public String aggregateRootTypeName() {
        return _aggregateRootTypeName;
    }

    public void setAggregateRootTypeName(String aggregateRootTypeName) {
        _aggregateRootTypeName = aggregateRootTypeName;
    }

    public String aggregateRootId() {
        return _aggregateRootId;
    }

    public void setAggregateRootId(String aggregateRootId) {
        _aggregateRootId = aggregateRootId;
    }

    public int version() {
        return _version;
    }

    public void setVersion(int version) {
        _version = version;
    }

    public List<IDomainEvent> events() {
        return _events;
    }

    public void setEvents(List<IDomainEvent> events) {
        _events = events;
    }

    public Date timestamp() {
        return _timestamp;
    }

    public void setTimestamp(Date timestamp) {
        _timestamp = timestamp;
    }

    public Map<String, String> items() {
        return _items;
    }

    public void setItems(Map<String, String> items) {
        this._items = items;
    }

    @Override
    public String toString() {
        String format = "[CommandId=%s,AggregateRootTypeName=%s,AggregateRootId=%s,Version=%d,Timestamp=%tc,Events=%s,Items=%s]";
        return String.format(format,
                _commandId,
                _aggregateRootTypeName,
                _aggregateRootId,
                _version,
                _timestamp,
                String.join("|", _events.stream().map(x -> x.getClass().getSimpleName()).collect(Collectors.toList())),
                String.join("|", _items.entrySet().stream().map(x -> x.getKey() + ":" + x.getValue()).collect(Collectors.toList())));
    }
}
