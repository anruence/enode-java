package org.enodeframework.infrastructure.impl;

import org.enodeframework.annotation.Command;
import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Priority;
import org.enodeframework.annotation.Subscribe;
import org.enodeframework.common.container.IObjectContainer;
import org.enodeframework.common.exception.EnodeRuntimeException;
import org.enodeframework.common.exception.RegisterComponentException;
import org.enodeframework.infrastructure.IAssemblyInitializer;
import org.enodeframework.infrastructure.IObjectProxy;
import org.enodeframework.infrastructure.MethodInvocation;
import org.enodeframework.messaging.MessageHandlerData;
import org.reflections.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractHandlerProvider<TKey, THandlerProxyInterface extends IObjectProxy & MethodInvocation, THandlerSource> implements IAssemblyInitializer {
    private final Map<TKey, List<THandlerProxyInterface>> handlerDict = new HashMap<>();
    private final Map<TKey, MessageHandlerData<THandlerProxyInterface>> messageHandlerDict = new HashMap<>();
    private final MethodHandles.Lookup lookup = MethodHandles.lookup();

    protected abstract TKey getKey(Method method);

    protected abstract Class<? extends THandlerProxyInterface> getHandlerProxyImplementationType();

    protected abstract boolean isHandlerSourceMatchKey(THandlerSource handlerSource, TKey key);

    protected abstract boolean isHandleMethodMatch(Method method);

    protected abstract IObjectContainer getObjectContainer();

    @Override
    public void initialize(Set<Class<?>> componentTypes) {
        componentTypes.stream().filter(this::isHandlerType).forEach(this::registerHandler);
        initializeHandlerPriority();
    }

    public List<MessageHandlerData<THandlerProxyInterface>> getHandlers(THandlerSource source) {
        List<MessageHandlerData<THandlerProxyInterface>> handlerDataList = new ArrayList<>();
        messageHandlerDict.keySet().stream()
                .filter(key -> isHandlerSourceMatchKey(source, key))
                .forEach(key -> handlerDataList.add(messageHandlerDict.get(key)));
        return handlerDataList;
    }

    private void initializeHandlerPriority() {
        handlerDict.forEach((key, handlers) -> {
            MessageHandlerData<THandlerProxyInterface> handlerData = new MessageHandlerData<>();
            List<THandlerProxyInterface> listHandlers = new ArrayList<>();
            Map<THandlerProxyInterface, Integer> queueHandlerDict = new HashMap<>();
            handlers.forEach(handler -> {
                int priority = getHandleMethodPriority(handler);
                if (priority == 0) {
                    listHandlers.add(handler);
                } else {
                    queueHandlerDict.put(handler, priority);
                }
            });
            handlerData.allHandlers = handlers;
            handlerData.listHandlers = listHandlers;
            handlerData.queuedHandlers = queueHandlerDict.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).map(x -> x.getKey()).collect(Collectors.toList());
            messageHandlerDict.put(key, handlerData);
        });
    }

    private int getHandleMethodPriority(THandlerProxyInterface handler) {
        Method method = handler.getMethod();
        int priority = 0;
        Priority methodPriority = method.getAnnotation(Priority.class);
        if (methodPriority != null) {
            priority = methodPriority.value();
        }
        if (priority == 0) {
            Priority classPriority = handler.getInnerObject().getClass().getAnnotation(Priority.class);
            if (classPriority != null) {
                priority = classPriority.value();
            }
        }
        return priority;
    }

    private boolean isHandlerType(Class<?> type) {
        if (type == null) {
            return false;
        }
        if (type.isInterface()) {
            return false;
        }
        if (Modifier.isAbstract(type.getModifiers())) {
            return false;
        }
        return containsHandleType(type);
    }

    private boolean containsHandleType(Class<?> type) {
        return type.isAnnotationPresent(Command.class) || type.isAnnotationPresent(Event.class);
    }

    protected boolean isMethodAnnotationSubscribe(Method method) {
        return method.isAnnotationPresent(Subscribe.class);
    }

    private void registerHandler(Class<?> handlerType) {
        Set<Method> handleMethods = ReflectionUtils.getMethods(handlerType, this::isHandleMethodMatch);
        handleMethods.forEach(method -> {
            try {
                //反射Method转换为MethodHandle,提高效率
                MethodHandle handleMethod = lookup.findVirtual(handlerType, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
                TKey key = getKey(method);
                List<THandlerProxyInterface> handlers = handlerDict.computeIfAbsent(key, k -> new ArrayList<>());
                IObjectContainer objectContainer = getObjectContainer();
                if (objectContainer == null) {
                    throw new IllegalArgumentException("IObjectContainer is null");
                }
                // prototype
                THandlerProxyInterface handlerProxy = objectContainer.resolve(getHandlerProxyImplementationType());
                if (handlerProxy == null) {
                    throw new EnodeRuntimeException("THandlerProxyInterface is null, " + getHandlerProxyImplementationType().getName());
                }
                handlerProxy.setHandlerType(handlerType);
                handlerProxy.setMethod(method);
                handlerProxy.setMethodHandle(handleMethod);
                handlers.add(handlerProxy);
            } catch (Exception e) {
                throw new RegisterComponentException(e);
            }
        });
    }
}
