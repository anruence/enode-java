package org.enodeframework.messaging.impl;

import org.enodeframework.infrastructure.IObjectProxy;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.enodeframework.messaging.IMessage;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SingleMessageDispatching {
    private final ConcurrentHashMap<String, IObjectProxy> handlerDict;
    private final QueueMessageDispatching queueMessageDispatching;
    private final IMessage message;

    public SingleMessageDispatching(IMessage message, QueueMessageDispatching queueMessageDispatching, List<? extends IObjectProxy> handlers, ITypeNameProvider typeNameProvider) {
        this.message = message;
        this.queueMessageDispatching = queueMessageDispatching;
        this.handlerDict = new ConcurrentHashMap<>();
        handlers.forEach(x -> handlerDict.putIfAbsent(typeNameProvider.getTypeName(x.getInnerObject().getClass()), x));
    }

    public void removeHandledHandler(String handlerTypeName) {
        if (handlerDict.remove(handlerTypeName) != null) {
            if (handlerDict.isEmpty()) {
                queueMessageDispatching.onMessageHandled(message);
            }
        }
    }

    public IMessage getMessage() {
        return message;
    }
}
