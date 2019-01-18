package com.enode.domain.impl;

import com.enode.common.function.Action2;
import com.enode.domain.IAggregateRoot;
import com.enode.domain.IAggregateRootInternalHandlerProvider;
import com.enode.eventing.IDomainEvent;
import com.enode.infrastructure.IAssemblyInitializer;
import com.enode.infrastructure.TypeUtils;
import com.enode.infrastructure.WrappedRuntimeException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultAggregateRootInternalHandlerProvider implements IAggregateRootInternalHandlerProvider, IAssemblyInitializer {

    private static final String HANDLE_METHOD_NAME = "handle";

    private Map<Class, Map<Class, Action2<IAggregateRoot, IDomainEvent>>> _mappings = new HashMap<>();

    @Override
    public void initialize(Set<Class<?>> componentTypes) {
        componentTypes.stream().filter(TypeUtils::isAggregateRoot).forEach(this::recurseRegisterInternalHandler);
    }

    private void recurseRegisterInternalHandler(Class aggregateRootType) {
        Class superclass = aggregateRootType.getSuperclass();

        if(!isInterfaceOrObjectClass(superclass)) {
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
                        method.getName().equals(HANDLE_METHOD_NAME)
                                && method.getParameterTypes().length == 1
                                && IDomainEvent.class.isAssignableFrom(method.getParameterTypes()[0])
                )
                .forEach(method -> registerInternalHandler(aggregateRootType, method.getParameterTypes()[0], method)
                );
    }

    private void registerInternalHandler(Class aggregateRootType, Class eventType, Method method) {
        Map<Class, Action2<IAggregateRoot, IDomainEvent>> eventHandlerDic = _mappings.computeIfAbsent(aggregateRootType, k -> new HashMap<>());

        try {
            //转换为MethodHandle提高效率
            //MethodHandle methodHandle = MethodHandles.lookup().findVirtual(aggregateRootType, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
            method.setAccessible(true);
            MethodHandle methodHandle = MethodHandles.lookup().unreflect(method);

            eventHandlerDic.put(eventType, (aggregateRoot, domainEvent) -> {
                try {
                    methodHandle.invoke(aggregateRoot, domainEvent);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            });
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    @Override
    public Action2<IAggregateRoot, IDomainEvent> getInternalEventHandler(Class<? extends IAggregateRoot> aggregateRootType,
                                                                         Class<? extends IDomainEvent> anEventType) {

        Map<Class, Action2<IAggregateRoot, IDomainEvent>> eventHandlerDic = _mappings.get(aggregateRootType);

        if (eventHandlerDic == null) {
            return null;
        }

        return eventHandlerDic.get(anEventType);
    }
}
