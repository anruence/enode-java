package org.enodeframework.eventing.impl

import com.google.common.collect.Maps
import org.enodeframework.common.serializing.ISerializeService
import org.enodeframework.eventing.IDomainEvent
import org.enodeframework.eventing.IEventSerializer
import org.enodeframework.infrastructure.ITypeNameProvider

/**
 * @author anruence@gmail.com
 */
class DefaultEventSerializer(
    private val typeNameProvider: ITypeNameProvider,
    private val serializeService: ISerializeService
) : IEventSerializer {
    override fun serialize(evnts: List<IDomainEvent<*>>): Map<String, String> {
        val dict = Maps.newLinkedHashMap<String, String>()
        evnts.forEach { evnt: IDomainEvent<*> ->
            val typeName = typeNameProvider.getTypeName(evnt.javaClass)
            val eventData = serializeService.serialize(evnt)
            dict[typeName] = eventData
        }
        return dict
    }

    override fun deserialize(data: Map<String, String>): List<IDomainEvent<*>> {
        val evnts: MutableList<IDomainEvent<*>> = ArrayList()
        data.forEach { (key: String, value: String) ->
            val eventType = typeNameProvider.getType(key)
            val evnt = serializeService.deserialize(value, eventType) as IDomainEvent<*>
            evnts.add(evnt)
        }
        return evnts
    }
}