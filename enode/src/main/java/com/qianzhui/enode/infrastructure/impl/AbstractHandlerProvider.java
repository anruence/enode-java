package com.qianzhui.enode.infrastructure.impl;

import com.qianzhui.enode.common.container.IObjectContainer;
import com.qianzhui.enode.common.container.LifeStyle;
import com.qianzhui.enode.infrastructure.Component;
import com.qianzhui.enode.infrastructure.IAssemblyInitializer;
import com.qianzhui.enode.infrastructure.IObjectProxy;
import com.qianzhui.enode.infrastructure.MessageHandlerData;
import com.qianzhui.enode.infrastructure.MethodInvocation;
import com.qianzhui.enode.infrastructure.Priority;
import org.reflections.ReflectionUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractHandlerProvider<TKey, THandlerProxyInterface extends IObjectProxy & MethodInvocation, THandlerSource> implements IAssemblyInitializer {
    private Map<TKey, List<THandlerProxyInterface>> _handlerDict = new HashMap<>();
    private Map<TKey, MessageHandlerData<THandlerProxyInterface>> _messageHandlerDict = new HashMap<>();
    private MethodHandles.Lookup lookup = MethodHandles.lookup();

    /**
     * ICommandHandler、ICommandAsyncHandler、IMessageHandler、ITwoMessageHandler<,>、IThreeMessageHandler<,,>
     */
    protected abstract Class getGenericHandlerType();

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

        _messageHandlerDict.keySet().stream()
                .filter(key -> isHandlerSourceMatchKey(source, key))
                .forEach(key -> handlerDataList.add(_messageHandlerDict.get(key)));

        return handlerDataList;
    }

    private void initializeHandlerPriority() {
        _handlerDict.entrySet().forEach(entry -> {
            TKey key = entry.getKey();
            List<THandlerProxyInterface> handlers = entry.getValue();

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

            handlerData.AllHandlers = handlers;
            handlerData.ListHandlers = listHandlers;
            handlerData.QueuedHandlers = queueHandlerDict.entrySet().stream().sorted((o1, o2) -> o1.getValue() - o2.getValue()).map(x -> x.getKey()).collect(Collectors.toList());

            _messageHandlerDict.put(key, handlerData);
        });
    }

    private int getHandleMethodPriority(THandlerProxyInterface handler) {
        Method method = handler.getMethod();

        if ("handleAsync".equals(method.getName())) {
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
        } else {
            return 0;
        }
    }

    private boolean isHandlerType(Class type) {
        if (type == null) {
            return false;
        }
        if (type.isInterface()) {
            return false;
        }
        if (Modifier.isAbstract(type.getModifiers())) {
            return false;
        }
        Class gen = getGenericHandlerType();
        if (!gen.isAssignableFrom(type)) {
            return false;
        }
        return true;
    }

    private void registerHandler(Class handlerType) {
        LifeStyle lifeStyle = parseComponentLife(handlerType);
        Object handleObj = lifeStyle == LifeStyle.Singleton ? getObjectContainer().resolve(handlerType) : null;

        Set<Method> handleMethods = ReflectionUtils.getMethods(handlerType, this::isHandleMethodMatch);

        handleMethods.forEach(method -> {
            try {
                //反射Method转换为MethodHandle,提高效率

                MethodHandle handleMethod = lookup.findVirtual(handlerType, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()));
                TKey key = getKey(method);

                List<THandlerProxyInterface> handlers = _handlerDict.get(key);
                if (handlers == null) {
                    handlers = new ArrayList<>();
                    _handlerDict.put(key, handlers);
                }

                //TODO handle duplicate message
                /*var handler = handlers.SingleOrDefault(x => x.GetInnerObject().GetType() == handlerType);
                if (handler != null)
                {
                    throw new InvalidOperationException("Handler cannot handle duplicate message, handlerType:" + handlerType);
                }*/

                THandlerProxyInterface handlerProxy = getHandlerProxyImplementationType().getConstructor(IObjectContainer.class, Class.class, getGenericHandlerType(), MethodHandle.class, Method.class).newInstance(getObjectContainer(), handlerType, handleObj, handleMethod, method);
                handlers.add(handlerProxy);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static LifeStyle parseComponentLife(Class type) {
        Component annotation = (Component) type.getAnnotation(Component.class);

        if (annotation != null) {
            return annotation.life();
        }

        return LifeStyle.Singleton;
    }

//    x -> {
//        if (!x.getName().equals("handle"))
//            return false;
//
//        Class<?>[] parameterTypes = x.getParameterTypes();
//        if (parameterTypes.length != 2)
//            return false;
//
//        return parameterTypes[0] == ICommandContext.class && ICommand.class.isAssignableFrom(parameterTypes[1]);
//    }
}
