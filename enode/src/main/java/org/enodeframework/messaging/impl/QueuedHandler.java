package org.enodeframework.messaging.impl;

import org.enodeframework.common.function.Action2;
import org.enodeframework.infrastructure.IObjectProxy;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class QueuedHandler<T extends IObjectProxy> {
    private final Action2<QueuedHandler<T>, T> dispatchToNextHandler;
    private final ConcurrentLinkedQueue<T> handlerQueue;

    public QueuedHandler(List<T> handlers, Action2<QueuedHandler<T>, T> dispatchToNextHandler) {
        handlerQueue = new ConcurrentLinkedQueue<>();
        handlerQueue.addAll(handlers);
        this.dispatchToNextHandler = dispatchToNextHandler;
    }

    public T dequeueHandler() {
        return handlerQueue.poll();
    }

    public void onHandlerFinished(T handler) {
        T nextHandler = dequeueHandler();
        if (nextHandler != null) {
            dispatchToNextHandler.apply(this, nextHandler);
        }
    }
}
