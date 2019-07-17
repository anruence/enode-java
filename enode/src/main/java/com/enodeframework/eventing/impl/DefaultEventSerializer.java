package com.enodeframework.eventing.impl;

import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.eventing.IDomainEvent;
import com.enodeframework.eventing.IEventSerializer;
import com.enodeframework.infrastructure.IMessage;
import com.enodeframework.infrastructure.ITypeNameProvider;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author anruence@gmail.com
 */
public class DefaultEventSerializer implements IEventSerializer {

    @Autowired
    private ITypeNameProvider typeNameProvider;

    @Override
    public Map<String, String> serialize(List<IDomainEvent> evnts) {
        Map<String, String> dict = new HashMap<String, String>();

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
        evnts.sort(Comparator.comparingInt(IMessage::getSequence));
        return evnts;
    }
}
