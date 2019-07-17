package com.enodeframework.eventing.impl;

import com.enodeframework.common.function.Action1;
import com.enodeframework.common.io.Task;
import com.enodeframework.eventing.EventCommittingContext;
import com.enodeframework.infrastructure.DefaultMailBox;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author anruence@gmail.com
 */
public class EventMailBox extends DefaultMailBox<EventCommittingContext, Boolean> {

    private ConcurrentHashMap<String, ConcurrentHashMap<String, Byte>> aggregateDictDict;

    public EventMailBox(String routingKey, int batchSize, Action1<List<EventCommittingContext>> handleMessageAction) {
        super(routingKey, batchSize, true, null, (x -> {
            handleMessageAction.apply(x);
            return Task.completedTask;
        }));
        aggregateDictDict = new ConcurrentHashMap<>();
    }

    @Override
    public void enqueueMessage(EventCommittingContext message) {
        ConcurrentHashMap<String, Byte> eventDict = aggregateDictDict.computeIfAbsent(message.getEventStream().getAggregateRootId(), x -> new ConcurrentHashMap<>());
        if (eventDict.putIfAbsent(message.getEventStream().getId(), (byte) 1) == null) {
            super.enqueueMessage(message);
        }
    }

    @Override
    public CompletableFuture<Void> completeMessage(EventCommittingContext message, Boolean result) {
        return super.completeMessage(message, result).thenAccept(x -> {
            removeEventCommittingContext(message);
        });
    }

    @Override
    protected List<EventCommittingContext> filterMessages(List<EventCommittingContext> messages) {
        List<EventCommittingContext> filterCommittingContextList = new ArrayList<>();
        if (messages != null && messages.size() > 0) {
            for (EventCommittingContext committingContext : messages) {
                if (containsEventCommittingContext(committingContext)) {
                    filterCommittingContextList.add(committingContext);
                }
            }
        }
        return filterCommittingContextList;
    }

    public boolean containsEventCommittingContext(EventCommittingContext eventCommittingContext) {
        ConcurrentHashMap<String, Byte> eventDict = aggregateDictDict.get(eventCommittingContext.getEventStream().getAggregateRootId());
        if (eventDict == null) {
            return false;
        }
        return eventDict.containsKey(eventCommittingContext.getEventStream().getId());
    }

    public void removeAggregateAllEventCommittingContexts(String aggregateRootId) {
        aggregateDictDict.remove(aggregateRootId);
    }

    public void removeEventCommittingContext(EventCommittingContext eventCommittingContext) {
        ConcurrentHashMap<String, Byte> eventDict = aggregateDictDict.get(eventCommittingContext.getEventStream().getAggregateRootId());
        if (eventDict != null) {
            eventDict.remove(eventCommittingContext.getEventStream().getId());
        }
    }
}
