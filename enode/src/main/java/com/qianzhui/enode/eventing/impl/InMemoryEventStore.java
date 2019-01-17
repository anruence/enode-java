package com.qianzhui.enode.eventing.impl;

import com.google.common.collect.Maps;
import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.common.io.AsyncTaskStatus;
import com.qianzhui.enode.eventing.DomainEventStream;
import com.qianzhui.enode.eventing.EventAppendResult;
import com.qianzhui.enode.eventing.IEventStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class InMemoryEventStore implements IEventStore {
    private static final boolean EDITING = true;
    private static final boolean UNEDITING = false;
    private ConcurrentMap<String, AggregateInfo> _aggregateInfoDict;
    private boolean _supportBatchAppendEvent;

    public InMemoryEventStore() {
        _aggregateInfoDict = new ConcurrentHashMap<>();
        _supportBatchAppendEvent = true;
    }

    @Override
    public boolean isSupportBatchAppendEvent() {
        return _supportBatchAppendEvent;
    }

    public void setSupportBatchAppendEvent(boolean supportBatchAppendEvent) {
        this._supportBatchAppendEvent = supportBatchAppendEvent;
    }

    public List<DomainEventStream> queryAggregateEvents(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion) {
        List<DomainEventStream> eventStreams = new ArrayList<>();

        AggregateInfo aggregateInfo = _aggregateInfoDict.get(aggregateRootId);
        if (aggregateInfo == null) {
            return eventStreams;
        }

        int min = minVersion > 1 ? minVersion : 1;
        int max = maxVersion < aggregateInfo.getCurrentVersion() ? maxVersion : aggregateInfo.getCurrentVersion();

        return aggregateInfo.getEventDict().entrySet()
                .stream()
                .filter(x -> x.getKey() >= min && x.getKey() <= max)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public CompletableFuture<AsyncTaskResult<EventAppendResult>> batchAppendAsync(List<DomainEventStream> eventStreams) {
        BatchAppendFuture batchAppendFuture = new BatchAppendFuture();

        eventStreams.stream().map(this::appendAsync).forEach(batchAppendFuture::addTask);

        return batchAppendFuture.result();
    }

    class BatchAppendFuture {

        private AtomicInteger index;
        private ConcurrentMap<Integer, CompletableFuture<AsyncTaskResult<EventAppendResult>>> taskDict = Maps.newConcurrentMap();
        private CompletableFuture<AsyncTaskResult<EventAppendResult>> result;

        public BatchAppendFuture() {
            index = new AtomicInteger(0);
            result = new CompletableFuture<>();
        }

        public void addTask(CompletableFuture<AsyncTaskResult<EventAppendResult>> task) {
            int i = index.get();

            taskDict.put(index.getAndIncrement(), task);
            task.thenAccept(t -> {
                if (t.getData() != EventAppendResult.Success) {
                    result.complete(t);
                    taskDict.clear();
                    return;
                }

                if (!result.isDone()) {
                    taskDict.remove(i);

                    if (taskDict.isEmpty()) {
                        result.complete(new AsyncTaskResult<EventAppendResult>(AsyncTaskStatus.Success, EventAppendResult.Success));
                    }
                }
            });
        }

        public CompletableFuture<AsyncTaskResult<EventAppendResult>> result() {
            return result;
        }
    }

    @Override
    public CompletableFuture<AsyncTaskResult<EventAppendResult>> appendAsync(DomainEventStream eventStream) {
        return CompletableFuture.supplyAsync(() -> new AsyncTaskResult<>(AsyncTaskStatus.Success, null, appends(eventStream)));
    }

    @Override
    public CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, int version) {
        return CompletableFuture.supplyAsync(() -> new AsyncTaskResult<DomainEventStream>(AsyncTaskStatus.Success, null, find(aggregateRootId, version)));
    }

    @Override
    public CompletableFuture<AsyncTaskResult<DomainEventStream>> findAsync(String aggregateRootId, String commandId) {
        return CompletableFuture.supplyAsync(() -> new AsyncTaskResult<>(AsyncTaskStatus.Success, null, find(aggregateRootId, commandId)));
    }

    @Override
    public CompletableFuture<AsyncTaskResult<List<DomainEventStream>>> queryAggregateEventsAsync(String aggregateRootId, String aggregateRootTypeName, int minVersion, int maxVersion) {
        return CompletableFuture.supplyAsync(() -> new AsyncTaskResult<>(AsyncTaskStatus.Success, null, queryAggregateEvents(aggregateRootId, aggregateRootTypeName, minVersion, maxVersion)));
    }

    private EventAppendResult appends(DomainEventStream eventStream) {
        AggregateInfo aggregateInfo = _aggregateInfoDict.computeIfAbsent(eventStream.aggregateRootId(), key -> new AggregateInfo());

        if (!aggregateInfo.tryEnterEditing()) {
            return EventAppendResult.DuplicateEvent;
        }

        try {
            if (eventStream.version() == aggregateInfo.getCurrentVersion() + 1) {
                aggregateInfo.getEventDict().put(eventStream.version(), eventStream);
                aggregateInfo.getCommandDict().put(eventStream.commandId(), eventStream);
                aggregateInfo.setCurrentVersion(eventStream.version());
                return EventAppendResult.Success;
            }
            return EventAppendResult.DuplicateEvent;
        } finally {
            aggregateInfo.exitEditing();
        }
    }

    private DomainEventStream find(String aggregateRootId, int version) {
        AggregateInfo aggregateInfo = _aggregateInfoDict.get(aggregateRootId);
        if (aggregateInfo == null) {
            return null;
        }

        return aggregateInfo.getEventDict().get(version);
    }

    private DomainEventStream find(String aggregateRootId, String commandId) {
        AggregateInfo aggregateInfo = _aggregateInfoDict.get(aggregateRootId);
        if (aggregateInfo == null) {
            return null;
        }

        return aggregateInfo.getCommandDict().get(commandId);
    }

    class AggregateInfo {
        public AtomicBoolean status = new AtomicBoolean(false);
        public int currentVersion;
        public ConcurrentMap<Integer, DomainEventStream> eventDict;
        public ConcurrentMap<String, DomainEventStream> commandDict;

        public AggregateInfo() {
            eventDict = new ConcurrentHashMap<>();
            commandDict = new ConcurrentHashMap<>();
        }

        public boolean tryEnterEditing() {
            return status.compareAndSet(UNEDITING, EDITING);
        }

        public void exitEditing() {
            status.getAndSet(UNEDITING);
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
