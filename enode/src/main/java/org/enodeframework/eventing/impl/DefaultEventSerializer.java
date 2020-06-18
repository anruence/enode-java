package org.enodeframework.eventing.impl;

import com.google.common.collect.Maps;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.eventing.IDomainEvent;
import org.enodeframework.eventing.IEventSerializer;
import org.enodeframework.infrastructure.ITypeNameProvider;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author anruence@gmail.com
 */
public class DefaultEventSerializer implements IEventSerializer {

    private final ITypeNameProvider typeNameProvider;

    public DefaultEventSerializer(ITypeNameProvider typeNameProvider) {
        this.typeNameProvider = typeNameProvider;
    }

    @Override
    public Map<String, String> serialize(List<IDomainEvent> evnts) {
        LinkedHashMap<String, String> dict = Maps.newLinkedHashMap();
        evnts.forEach(evnt -> {
            String typeName = typeNameProvider.getTypeName(evnt.getClass());
            String eventData = JsonTool.serialize(evnt);
            dict.put(typeName, eventData);
        });
        return dict;
    }

    @Override
    public <TEvent extends IDomainEvent> List<TEvent> deserialize(Map<String, String> data, Class<TEvent> domainEventType) {
        List<TEvent> evnts = new ArrayList<>();
        data.forEach((key, value) -> {
            Class eventType = typeNameProvider.getType(key);
            TEvent evnt = (TEvent) JsonTool.deserialize(value, eventType);
            evnts.add(evnt);
        });
        return evnts;
    }
}
