package org.enodeframework.eventing.impl;

import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.EventAppendResult;
import org.enodeframework.eventing.IEventStore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class InMemoryEventStore implements IEventStore {
    private static final boolean EDITING = true;
    private static final boolean UNEDITING = false;
    private final Object lockObj = new Object();
    private ConcurrentMap<String, AggregateInfo> aggregateInfoDict;

    public InMemoryEventStore() {
        this.aggregateInfoDict = new ConcurrentHashMap<>();
    }

    public List<DomainEventStream> queryAggregateEvents(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion) {
        List<DomainEventStream> eventStreams = new ArrayList<>();
        AggregateInfo aggregateInfo = aggregateInfoDict.get(aggregateRootId);
        if (aggregateInfo == null) {
            return eventStreams;
        }
        int min = Math.max(minVersion, 1);
        int max = Math.min(maxVersion, aggregateInfo.getCurrentVersion());
        return aggregateInfo.getEventDict().entrySet()
                .stream()
                .filter(x -> x.getKey() >= min && x.getKey() <= max)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<EventAppendResult> batchAppendAsync(List<DomainEventStream> eventStreams) {
        Map<String, List<DomainEventStream>> eventStreamDict = eventStreams.stream().distinct().collect(Collectors.groupingBy(DomainEventStream::getAggregateRootId));
        EventAppendResult eventAppendResult = new EventAppendResult();
        CompletableFuture<EventAppendResult> future = new CompletableFuture<>();
        for (Map.Entry<String, List<DomainEventStream>> entry : eventStreamDict.entrySet()) {
            batchAppend(entry.getKey(), entry.getValue(), eventAppendResult);
        }
        future.complete(eventAppendResult);
        return future;
    }

    @Override
    public CompletableFuture<DomainEventStream> findAsync(String aggregateRootId, int version) {
        return CompletableFuture.completedFuture(find(aggregateRootId, version));
    }

    @Override
    public CompletableFuture<DomainEventStream> findAsync(String aggregateRootId, String commandId) {
        return CompletableFuture.completedFuture(find(aggregateRootId, commandId));
    }

    @Override
    public CompletableFuture<List<DomainEventStream>> queryAggregateEventsAsync(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion) {
        return CompletableFuture.completedFuture(queryAggregateEvents(aggregateRootId, aggregateRootTypeName, minVersion, maxVersion));
    }

    @Override
    public CompletableFuture<Integer> getPublishedVersionAsync(String processorName, String aggregateRootTypeName, String aggregateRootId) {
        AggregateInfo aggregateInfo = aggregateInfoDict.get(aggregateRootId);
        if (aggregateInfo == null) {
            return CompletableFuture.completedFuture(0);
        }
        int version = aggregateInfo.getEventDict().entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .map(x -> x.getVersion())
                .max(Comparator.naturalOrder()).orElse(0);
        return CompletableFuture.completedFuture(version);
    }

    private DomainEventStream find(String aggregateRootId, int version) {
        AggregateInfo aggregateInfo = aggregateInfoDict.getOrDefault(aggregateRootId, null);
        if (aggregateInfo == null) {
            return null;
        }
        return aggregateInfo.getEventDict().get(version);
    }

    private DomainEventStream find(String aggregateRootId, String commandId) {
        AggregateInfo aggregateInfo = aggregateInfoDict.getOrDefault(aggregateRootId, null);
        if (aggregateInfo == null) {
            return null;
        }
        return aggregateInfo.getCommandDict().get(commandId);
    }

    private void batchAppend(String aggregateRootId, List<DomainEventStream> eventStreamList, EventAppendResult eventAppendResult) {
        synchronized (lockObj) {
            AggregateInfo aggregateInfo = aggregateInfoDict.computeIfAbsent(aggregateRootId, x -> new AggregateInfo());
            Optional<DomainEventStream> optionalDomainEventStream = eventStreamList.stream().findFirst();
            if (optionalDomainEventStream.isPresent()) {
                DomainEventStream firstEventStream = optionalDomainEventStream.get();
                //检查提交过来的第一个事件的版本号是否是当前聚合根的当前版本号的下一个版本号
                if (firstEventStream.getVersion() != aggregateInfo.getCurrentVersion() + 1) {
                    eventAppendResult.addDuplicateEventAggregateRootId(aggregateRootId);
                    return;
                }
            }
            //检查提交过来的事件本身是否满足版本号的递增关系
            for (int i = 0; i < eventStreamList.size() - 1; i++) {
                if (eventStreamList.get(i + 1).getVersion() != eventStreamList.get(i).getVersion() + 1) {
                    eventAppendResult.addDuplicateEventAggregateRootId(aggregateRootId);
                    return;
                }
            }

            //检查重复处理的命令ID
            List<String> duplicateCommandIds = new ArrayList<>();

            for (DomainEventStream eventStream : eventStreamList) {
                if (aggregateInfo.getCommandDict().containsKey(eventStream.getCommandId())) {
                    duplicateCommandIds.add(eventStream.getCommandId());
                }
            }
            if (duplicateCommandIds.size() > 0) {
                eventAppendResult.addDuplicateCommandIds(aggregateRootId, duplicateCommandIds);
                return;
            }

            for (DomainEventStream eventStream : eventStreamList) {
                aggregateInfo.getEventDict().put(eventStream.getVersion(), eventStream);
                aggregateInfo.getCommandDict().put(eventStream.getCommandId(), eventStream);
                aggregateInfo.setCurrentVersion(eventStream.getVersion());
            }

            if (!eventAppendResult.getSuccessAggregateRootIdList().contains(aggregateRootId)) {
                eventAppendResult.addSuccessAggregateRootId(aggregateRootId);
            }
        }
    }

    class AggregateInfo {
        public AtomicBoolean status = new AtomicBoolean(false);
        public int currentVersion;
        public ConcurrentMap<Integer, DomainEventStream> eventDict;
        public ConcurrentMap<String, DomainEventStream> commandDict;

        public AggregateInfo() {
            this.eventDict = new ConcurrentHashMap<>();
            this.commandDict = new ConcurrentHashMap<>();
        }

        public int getCurrentVersion() {
            return currentVersion;
        }

        public void setCurrentVersion(int currentVersion) {
            this.currentVersion = currentVersion;
        }

        public ConcurrentMap<Integer, DomainEventStream> getEventDict() {
            return eventDict;
        }

        public void setEventDict(ConcurrentMap<Integer, DomainEventStream> eventDict) {
            this.eventDict = eventDict;
        }

        public ConcurrentMap<String, DomainEventStream> getCommandDict() {
            return commandDict;
        }

        public void setCommandDict(ConcurrentMap<String, DomainEventStream> commandDict) {
            this.commandDict = commandDict;
        }
    }
}
