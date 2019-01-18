package com.enode.eventing.impl;

import com.enode.common.serializing.IJsonSerializer;
import com.enode.eventing.IDomainEvent;
import com.enode.eventing.IEventSerializer;
import com.enode.infrastructure.IMessage;
import com.enode.infrastructure.ITypeNameProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultEventSerializer implements IEventSerializer {
    private ITypeNameProvider _typeNameProvider;
    private IJsonSerializer _jsonSerializer;

    @Inject
    public DefaultEventSerializer(ITypeNameProvider typeNameProvider, IJsonSerializer jsonSerializer) {
        _typeNameProvider = typeNameProvider;
        _jsonSerializer = jsonSerializer;
    }

    @Override
    public Map<String, String> serialize(List<IDomainEvent> evnts) {
        Map<String, String> dict = new HashMap<String, String>();

        evnts.forEach(evnt -> {
            String typeName = _typeNameProvider.getTypeName(evnt.getClass());
            String eventData = _jsonSerializer.serialize(evnt);
            dict.put(typeName, eventData);
        });

        return dict;
    }

    @Override
    public <TEvent extends IDomainEvent> List<TEvent> deserialize(Map<String, String> data, Class<TEvent> domainEventType) {
        List<TEvent> evnts = new ArrayList<>();
        data.forEach((key, value) -> {
            Class eventType = _typeNameProvider.getType(key);
            TEvent evnt = (TEvent) _jsonSerializer.deserialize(value, eventType);
            evnts.add(evnt);
        });
        evnts.sort(Comparator.comparingInt(IMessage::sequence));
        return evnts;
    }
}
