package org.enodeframework.domain.impl;

import org.enodeframework.common.SysProperties;
import org.enodeframework.common.exception.HandlerRegisterException;
import org.enodeframework.common.exception.MethodInvokeException;
import org.enodeframework.common.function.Action2;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IAggregateRootInternalHandlerProvider;
import org.enodeframework.eventing.IDomainEvent;
import org.enodeframework.infrastructure.IAssemblyInitializer;
import org.enodeframework.infrastructure.TypeUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author anruence@gmail.com
 */
public class DefaultAggregateRootInternalHandlerProvider implements IAggregateRootInternalHandlerProvider, IAssemblyInitializer {
    private final Map<Class<?>, Map<Class<?>, Action2<IAggregateRoot, IDomainEvent<?>>>> MAPPINGS = new HashMap<>();

    @Override
    public void initialize(Set<Class<?>> componentTypes) {
        componentTypes.stream().filter(TypeUtils::isAggregateRoot).forEach(this::recurseRegisterInternalHandler);
    }

    private void recurseRegisterInternalHandler(Class<?> aggregateRootType) {
        Class<?> superclass = aggregateRootType.getSuperclass();
        if (!isInterfaceOrObjectClass(superclass)) {
            registerInternalHandlerWithSuperclass(aggregateRootType, superclass);
        }
        register(aggregateRootType, aggregateRootType);
    }

    private void registerInternalHandlerWithSuperclass(Class<?> aggregateRootType, Class<?> parentType) {
        Class<?> superclass = parentType.getSuperclass();
        if (!isInterfaceOrObjectClass(superclass)) {
            registerInternalHandlerWithSuperclass(aggregateRootType, superclass);
        }
        register(aggregateRootType, parentType);
    }

    private boolean isInterfaceOrObjectClass(Class<?> type) {
        return Modifier.isInterface(type.getModifiers()) || type.equals(Object.class);
    }

    private void register(Class<?> aggregateRootType, Class<?> type) {
        Arrays.stream(type.getDeclaredMethods())
                .filter(method -> method.getName().equalsIgnoreCase(SysProperties.AGGREGATE_ROOT_HANDLE_METHOD_NAME)
                        && method.getParameterTypes().length == 1
                        && IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[0]))
                .forEach(method -> registerInternalHandler(aggregateRootType, method.getParameterTypes()[0], method));
    }

    private void registerInternalHandler(Class<?> aggregateRootType, Class<?> eventType, Method method) {
        Map<Class<?>, Action2<IAggregateRoot, IDomainEvent<?>>> eventHandlerDic = MAPPINGS.computeIfAbsent(aggregateRootType, k -> new HashMap<>());
        method.setAccessible(true);
        try {
            MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);
            eventHandlerDic.put(eventType, (aggregateRoot, domainEvent) -> {
                try {
                    methodHandle.invoke(aggregateRoot, domainEvent);
                } catch (Throwable throwable) {
                    throw new MethodInvokeException(throwable);
                }
            });
        } catch (IllegalAccessException e) {
            throw new HandlerRegisterException(e);
        }
    }

    @Override
    public Action2<IAggregateRoot, IDomainEvent<?>> getInternalEventHandler(Class<? extends IAggregateRoot> aggregateRootType, Class<? extends IDomainEvent> eventType) {
        Class currentAggregateType = aggregateRootType;
        while (currentAggregateType != null) {
            Action2<IAggregateRoot, IDomainEvent<?>> handler = getEventHandler(currentAggregateType, eventType);
            if (handler != null) {
                return handler;
            }
            if (currentAggregateType.getSuperclass() != null && Arrays.asList(currentAggregateType.getSuperclass().getInterfaces()).contains(IAggregateRoot.class)) {
                currentAggregateType = currentAggregateType.getSuperclass();
            } else {
                break;
            }
        }
        return null;
    }

    private Action2<IAggregateRoot, IDomainEvent<?>> getEventHandler(Class<? extends IAggregateRoot> aggregateRootType, Class<? extends IDomainEvent> anEventType) {
        Map<Class<?>, Action2<IAggregateRoot, IDomainEvent<?>>> eventHandlerDic = MAPPINGS.get(aggregateRootType);
        if (eventHandlerDic == null) {
            return null;
        }
        return eventHandlerDic.get(anEventType);
    }
}
