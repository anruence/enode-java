package com.enode.eventing;

import java.util.List;
import java.util.Map;

public interface IEventSerializer {
    Map<String, String> serialize(List<IDomainEvent> evnts);

    <TEvent extends IDomainEvent> List<TEvent> deserialize(Map<String, String> data, Class<TEvent> domainEventType);
}
