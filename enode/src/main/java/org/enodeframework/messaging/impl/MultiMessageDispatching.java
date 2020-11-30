package org.enodeframework.messaging.impl;

import org.enodeframework.infrastructure.IObjectProxy;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.enodeframework.messaging.IMessage;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MultiMessageDispatching {
    private final IMessage[] messages;
    private final ConcurrentMap<String, IObjectProxy> handlerDict;
    private final RootDispatching rootDispatching;

    public MultiMessageDispatching(List<? extends IMessage> messages, List<? extends IObjectProxy> handlers, RootDispatching rootDispatching, ITypeNameProvider typeNameProvider) {
        this.messages = messages.toArray(new IMessage[0]);
        handlerDict = new ConcurrentHashMap<>();
        handlers.forEach(x -> handlerDict.putIfAbsent(typeNameProvider.getTypeName(x.getInnerObject().getClass()), x));
        this.rootDispatching = rootDispatching;
        this.rootDispatching.addChildDispatching(this);
    }

    public IMessage[] getMessages() {
        return messages;
    }

    public void removeHandledHandler(String handlerTypeName) {
        if (handlerDict.remove(handlerTypeName) != null) {
            if (handlerDict.isEmpty()) {
                rootDispatching.onChildDispatchingFinished(this);
            }
        }
    }
}
