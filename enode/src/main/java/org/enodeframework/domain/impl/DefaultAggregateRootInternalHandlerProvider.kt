package org.enodeframework.domain.impl

import org.enodeframework.common.SysProperties
import org.enodeframework.common.exception.HandlerNotFoundException
import org.enodeframework.common.function.Action2
import org.enodeframework.domain.IAggregateRoot
import org.enodeframework.domain.IAggregateRootInternalHandlerProvider
import org.enodeframework.eventing.IDomainEvent
import org.enodeframework.infrastructure.IAssemblyInitializer
import org.enodeframework.infrastructure.TypeUtils
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

/**
 * @author anruence@gmail.com
 */
class DefaultAggregateRootInternalHandlerProvider : IAggregateRootInternalHandlerProvider, IAssemblyInitializer {

    private val AGGREGATE_ROOT_HANDLER_DICT: MutableMap<Class<*>, MutableMap<Class<*>, Action2<IAggregateRoot, IDomainEvent<*>>>> =
        HashMap()

    override fun initialize(componentTypes: Set<Class<*>>) {
        componentTypes.stream().filter { type: Class<*> -> TypeUtils.isAggregateRoot(type) }
            .forEach { aggregateRootType: Class<*> -> recurseRegisterInternalHandler(aggregateRootType) }
    }

    private fun recurseRegisterInternalHandler(aggregateRootType: Class<*>) {
        val superclass = aggregateRootType.superclass
        if (!isInterfaceOrObjectClass(superclass)) {
            registerInternalHandlerWithSuperclass(aggregateRootType, superclass)
        }
        register(aggregateRootType, aggregateRootType)
    }

    private fun registerInternalHandlerWithSuperclass(aggregateRootType: Class<*>, parentType: Class<*>) {
        val superclass = parentType.superclass
        if (!isInterfaceOrObjectClass(superclass)) {
            registerInternalHandlerWithSuperclass(aggregateRootType, superclass)
        }
        register(aggregateRootType, parentType)
    }

    private fun isInterfaceOrObjectClass(type: Class<*>): Boolean {
        return Modifier.isInterface(type.modifiers) || type == Any::class.java
    }

    private fun register(aggregateRootType: Class<*>, type: Class<*>) {
        Arrays.stream(type.declaredMethods).filter { method: Method ->
            method.name.startsWith(SysProperties.AGGREGATE_ROOT_HANDLE_METHOD_NAME)
                    && method.parameterTypes.size == 1 && IDomainEvent::class.java.isAssignableFrom(method.parameterTypes[0])
        }.forEach { method: Method ->
            registerInternalHandler(aggregateRootType, method.parameterTypes[0], method)
        }
    }

    private fun registerInternalHandler(aggregateRootType: Class<*>, eventType: Class<*>, method: Method) {
        val eventHandlerDic = AGGREGATE_ROOT_HANDLER_DICT.computeIfAbsent(aggregateRootType) { HashMap() }
        method.isAccessible = true
        val methodHandle = MethodHandles.lookup().unreflect(method)
        eventHandlerDic[eventType] = Action2 { aggregateRoot: IAggregateRoot, domainEvent: IDomainEvent<*> ->
            methodHandle.invoke(aggregateRoot, domainEvent)
        }
    }

    override fun getInternalEventHandler(
        aggregateRootType: Class<out IAggregateRoot>,
        eventType: Class<out IDomainEvent<*>>
    ): Action2<IAggregateRoot, IDomainEvent<*>> {
        var currentAggregateType = aggregateRootType
        while (true) {
            val handler = AGGREGATE_ROOT_HANDLER_DICT[currentAggregateType]?.get(eventType)
            if (handler != null) {
                return handler
            }
            currentAggregateType = if (currentAggregateType.superclass != null
                && listOf(*currentAggregateType.superclass.interfaces).contains(IAggregateRoot::class.java)
            ) {
                currentAggregateType.superclass as Class<out IAggregateRoot>
            } else {
                break
            }
        }
        throw HandlerNotFoundException(
            String.format(
                "Could not find event handler for [%s] of [%s]",
                eventType.javaClass.name,
                aggregateRootType.javaClass.name
            )
        )
    }
}