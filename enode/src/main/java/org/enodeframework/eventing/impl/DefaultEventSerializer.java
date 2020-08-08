package org.enodeframework.eventing.impl;

import com.google.common.collect.Maps;
import org.enodeframework.common.serializing.ISerializeService;
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

    private final ISerializeService serializeService;

    public DefaultEventSerializer(ITypeNameProvider typeNameProvider, ISerializeService serializeService) {
        this.typeNameProvider = typeNameProvider;
        this.serializeService = serializeService;
    }

    @Override
    public Map<String, String> serialize(List<IDomainEvent<?>> evnts) {
        LinkedHashMap<String, String> dict = Maps.newLinkedHashMap();
        evnts.forEach(evnt -> {
            String typeName = typeNameProvider.getTypeName(evnt.getClass());
            String eventData = serializeService.serialize(evnt);
            dict.put(typeName, eventData);
        });
        return dict;
    }

    @Override
    public List<IDomainEvent<?>> deserialize(Map<String, String> data) {
        List<IDomainEvent<?>> evnts = new ArrayList<>();
        data.forEach((key, value) -> {
            Class<?> eventType = typeNameProvider.getType(key);
            IDomainEvent<?> evnt = (IDomainEvent<?>) serializeService.deserialize(value, eventType);
            evnts.add(evnt);
        });
        return evnts;
    }
}
