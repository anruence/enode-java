package org.enodeframework.messaging.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class RootDispatching {
    private final CompletableFuture<Boolean> taskCompletionSource;
    private final ConcurrentHashMap<Object, Boolean> childDispatchingDict;

    public RootDispatching() {
        taskCompletionSource = new CompletableFuture<>();
        childDispatchingDict = new ConcurrentHashMap<>();
    }

    public CompletableFuture<Boolean> getTaskCompletionSource() {
        return taskCompletionSource;
    }

    public void addChildDispatching(Object childDispatching) {
        childDispatchingDict.put(childDispatching, false);
    }

    public void onChildDispatchingFinished(Object childDispatching) {
        if (childDispatchingDict.remove(childDispatching) != null) {
            if (childDispatchingDict.isEmpty()) {
                taskCompletionSource.complete(true);
            }
        }
    }
}
