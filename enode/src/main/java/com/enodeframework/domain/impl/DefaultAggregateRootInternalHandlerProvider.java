package com.enodeframework.domain.impl;

import com.enodeframework.common.function.Action2;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.domain.IAggregateRootInternalHandlerProvider;
import com.enodeframework.eventing.IDomainEvent;
import com.enodeframework.infrastructure.IAssemblyInitializer;
import com.enodeframework.infrastructure.TypeUtils;
import com.enodeframework.infrastructure.WrappedRuntimeException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.enodeframework.common.SysProperties.AGGREGATE_ROOT_HANDLE_METHOD_NAME;

/**
 * @author anruence@gmail.com
 */
public class DefaultAggregateRootInternalHandlerProvider implements IAggregateRootInternalHandlerProvider, IAssemblyInitializer {

    private Map<Class, Map<Class, Action2<IAggregateRoot, IDomainEvent>>> mappings = new HashMap<>();

    @Override
    public void initialize(Set<Class<?>> componentTypes) {
        componentTypes.stream().filter(TypeUtils::isAggregateRoot).forEach(this::recurseRegisterInternalHandler);
    }

    private void recurseRegisterInternalHandler(Class aggregateRootType) {
        Class superclass = aggregateRootType.getSuperclass();

        if (!isInterfaceOrObjectClass(superclass)) {
            registerInternalHandlerWithSuperclass(aggregateRootType, superclass);
        }

        register(aggregateRootType, aggregateRootType);
    }

    private void registerInternalHandlerWithSuperclass(Class aggregateRootType, Class parentType) {
        Class superclass = parentType.getSuperclass();

        if (!isInterfaceOrObjectClass(superclass)) {
            registerInternalHandlerWithSuperclass(aggregateRootType, superclass);
        }

        register(aggregateRootType, parentType);

    }

    private boolean isInterfaceOrObjectClass(Class type) {
        return Modifier.isInterface(type.getModifiers()) || type.equals(Object.class);
    }

    private void register(Class aggregateRootType, Class type) {
        Arrays.asList(type.getDeclaredMethods()).stream()
                .filter(method ->
                        method.getName().equalsIgnoreCase(AGGREGATE_ROOT_HANDLE_METHOD_NAME)
                                && method.getParameterTypes().length == 1
                                && IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[0])
                )
                .forEach(method -> registerInternalHandler(aggregateRootType, method.getParameterTypes()[0], method)
                );
    }

    private void registerInternalHandler(Class aggregateRootType, Class eventType, Method method) {
        Map<Class, Action2<IAggregateRoot, IDomainEvent>> eventHandlerDic = mappings.computeIfAbsent(aggregateRootType, k -> new HashMap<>());
        method.setAccessible(true);
        try {
            MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);
            eventHandlerDic.put(eventType, (aggregateRoot, domainEvent) -> {
                try {
                    methodHandle.invoke(aggregateRoot, domainEvent);
                } catch (Throwable throwable) {
                    throw new WrappedRuntimeException(throwable);
                }
            });
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    @Override
    public Action2<IAggregateRoot, IDomainEvent> getInternalEventHandler(Class<? extends IAggregateRoot> aggregateRootType, Class<? extends IDomainEvent> anEventType) {
        Class currentAggregateType = aggregateRootType;
        while (currentAggregateType != null) {
            Action2<IAggregateRoot, IDomainEvent> handler = getEventHandler(currentAggregateType, anEventType);
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

    private Action2<IAggregateRoot, IDomainEvent> getEventHandler(Class<? extends IAggregateRoot> aggregateRootType, Class<? extends IDomainEvent> anEventType) {
        Map<Class, Action2<IAggregateRoot, IDomainEvent>> eventHandlerDic = mappings.get(aggregateRootType);
        if (eventHandlerDic == null) {
            return null;
        }
        return eventHandlerDic.get(anEventType);
    }
}
