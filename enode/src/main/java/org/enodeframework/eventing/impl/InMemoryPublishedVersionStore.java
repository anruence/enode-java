package org.enodeframework.eventing.impl;

import org.enodeframework.eventing.IPublishedVersionStore;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author anruence@gmail.com
 */
public class InMemoryPublishedVersionStore implements IPublishedVersionStore {

    private final ConcurrentMap<String, Integer> versionDict = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Integer> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId) {
        int publishedVersion = versionDict.getOrDefault(buildKey(processorName, aggregateRootId), 0);
        return CompletableFuture.completedFuture(publishedVersion);
    }

    private String buildKey(String eventProcessorName, String aggregateRootId) {
        return String.format("%s-%s", eventProcessorName, aggregateRootId);
    }
}
