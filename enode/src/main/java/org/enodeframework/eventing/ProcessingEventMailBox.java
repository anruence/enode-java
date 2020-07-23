package org.enodeframework.eventing;

import org.enodeframework.common.exception.MailBoxProcessException;
import org.enodeframework.common.function.Action1;
import org.enodeframework.common.io.Task;
import org.enodeframework.messaging.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class ProcessingEventMailBox {
    private final static Logger logger = LoggerFactory.getLogger(ProcessingEventMailBox.class);

    private final Object lockObj = new Object();

    private final String aggregateRootId;

    private final String aggregateRootTypeName;
    private final AtomicInteger isUsing = new AtomicInteger(0);
    private final AtomicInteger isRemoved = new AtomicInteger(0);
    private final AtomicInteger isRunning = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, ProcessingEvent> waitingProcessingEventDict = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<ProcessingEvent> processingEventQueue;
    private final Action1<ProcessingEvent> handleProcessingEventAction;
    private Date lastActiveTime;
    private Integer nextExpectingEventVersion;

    public ProcessingEventMailBox(String aggregateRootTypeName, String aggregateRootId, Action1<ProcessingEvent> handleProcessingEventAction) {
        processingEventQueue = new ConcurrentLinkedQueue<>();
        this.aggregateRootId = aggregateRootId;
        this.aggregateRootTypeName = aggregateRootTypeName;
        this.handleProcessingEventAction = handleProcessingEventAction;
        lastActiveTime = new Date();
    }

    private void tryRemovedInvalidWaitingMessages(int version) {
        waitingProcessingEventDict.keySet().stream().filter(x -> x < version).forEach(key -> {
            if (waitingProcessingEventDict.containsKey(version)) {
                ProcessingEvent processingEvent = waitingProcessingEventDict.remove(key);
                logger.warn("{} invalid waiting message removed, aggregateRootType: {}, aggregateRootId: {}, commandId: {}, eventVersion: {}, eventStreamId: {}, eventTypes: {}, eventIds: {}, nextExpectingEventVersion: {}",
                        getClass().getName(),
                        processingEvent.getMessage().getAggregateRootTypeName(),
                        processingEvent.getMessage().getAggregateRootId(),
                        processingEvent.getMessage().getCommandId(),
                        processingEvent.getMessage().getVersion(),
                        processingEvent.getMessage().getId(),
                        processingEvent.getMessage().getEvents().stream().map(x -> x.getClass().getName()).collect(Collectors.joining("|")),
                        processingEvent.getMessage().getEvents().stream().map(IMessage::getId).collect(Collectors.joining("|")),
                        version);
            }
        });
    }

    private void tryEnqueueValidWaitingMessage() {
        if (this.nextExpectingEventVersion == null) {
            return;
        }
        while (waitingProcessingEventDict.containsKey(this.nextExpectingEventVersion)) {
            ProcessingEvent nextProcessingEvent = waitingProcessingEventDict.remove(this.nextExpectingEventVersion);
            if (nextProcessingEvent != null) {
                enqueueEventStream(nextProcessingEvent);
                logger.info("{} enqueued waiting processingEvent, aggregateRootId: {}, aggregateRootTypeName: {}, eventVersion: {}", getClass().getName(), aggregateRootId, aggregateRootTypeName, nextProcessingEvent.getMessage().getVersion());
            }
        }
    }

    public long getTotalUnHandledMessageCount() {
        return processingEventQueue.size();
    }

    public void setNextExpectingEventVersion(int version) {
        synchronized (lockObj) {
            tryRemovedInvalidWaitingMessages(version);
            if (this.nextExpectingEventVersion == null || version > this.nextExpectingEventVersion) {
                this.nextExpectingEventVersion = version;
                logger.info("{} refreshed nextExpectingEventVersion, aggregateRootId: {}, aggregateRootTypeName: {}, version: {}", getClass().getName(), aggregateRootId, aggregateRootTypeName, this.nextExpectingEventVersion);
                tryEnqueueValidWaitingMessage();
                lastActiveTime = new Date();
                tryRun();
            } else {
                logger.info("{} nextExpectingEventVersion ignored, aggregateRootId: {}, aggregateRootTypeName: {}, version: {}, current nextExpectingEventVersion: {}", getClass().getName(), aggregateRootId, aggregateRootTypeName, version, this.nextExpectingEventVersion);
            }
        }
    }

    private void enqueueEventStream(ProcessingEvent processingEvent) {
        synchronized (lockObj) {
            processingEvent.setMailbox(this);
            this.processingEventQueue.add(processingEvent);
            this.nextExpectingEventVersion = processingEvent.getMessage().getVersion() + 1;
            if (logger.isDebugEnabled()) {
                logger.debug("{} enqueued new message, aggregateRootType: {}, aggregateRootId: {}, commandId: {}, eventVersion: {}, eventStreamId: {}, eventTypes: {}, eventIds: {}",
                        getClass().getName(),
                        processingEvent.getMessage().getAggregateRootTypeName(),
                        processingEvent.getMessage().getAggregateRootId(),
                        processingEvent.getMessage().getCommandId(),
                        processingEvent.getMessage().getVersion(),
                        processingEvent.getMessage().getId(),
                        processingEvent.getMessage().getEvents().stream().map(x -> x.getClass().getName()).collect(Collectors.joining("|")),
                        processingEvent.getMessage().getEvents().stream().map(x -> x.getId()).collect(Collectors.joining("|"))
                );
            }
        }
    }

    public EnqueueMessageResult enqueueMessage(ProcessingEvent processingEvent) {
        synchronized (lockObj) {
            if (isRemoved()) {
                throw new MailBoxProcessException(String.format("ProcessingEventMailBox was removed, cannot allow to enqueue message, aggregateRootTypeName: %s, aggregateRootId: %s", aggregateRootTypeName, aggregateRootId));
            }
            DomainEventStreamMessage eventStream = processingEvent.getMessage();
            if (this.nextExpectingEventVersion == null || eventStream.getVersion() > this.nextExpectingEventVersion) {
                if (waitingProcessingEventDict.putIfAbsent(eventStream.getVersion(), processingEvent) == null) {
                    logger.warn("{} waiting message added, aggregateRootType: {}, aggregateRootId: {}, commandId: {}, eventVersion: {}, eventStreamId: {}, eventTypes: {}, eventIds: {}, nextExpectingEventVersion: {}",
                            getClass().getName(),
                            eventStream.getAggregateRootTypeName(),
                            eventStream.getAggregateRootId(),
                            eventStream.getCommandId(),
                            eventStream.getVersion(),
                            eventStream.getId(),
                            eventStream.getEvents().stream().map(x -> x.getClass().getName()).collect(Collectors.joining("|")),
                            eventStream.getEvents().stream().map(IMessage::getId).collect(Collectors.joining("|")),
                            this.nextExpectingEventVersion
                    );
                }
                return EnqueueMessageResult.AddToWaitingList;
            } else if (eventStream.getVersion() == this.nextExpectingEventVersion) {
                enqueueEventStream(processingEvent);
                tryEnqueueValidWaitingMessage();
                lastActiveTime = new Date();
                tryRun();
                return EnqueueMessageResult.Success;
            }
            return EnqueueMessageResult.Ignored;
        }
    }

    /**
     * 尝试运行一次MailBox，一次运行会处理一个消息或者一批消息，当前MailBox不能是运行中或者暂停中或者已暂停
     */
    public void tryRun() {
        synchronized (lockObj) {
            if (isRunning()) {
                return;
            }
            setAsRunning();
            if (logger.isDebugEnabled()) {
                logger.debug("{} start run, aggregateRootId: {}", getClass().getName(), aggregateRootId);
            }
            CompletableFuture.runAsync(this::processMessages);
        }
    }

    /**
     * 请求完成MailBox的单次运行，如果MailBox中还有剩余消息，则继续尝试运行下一次
     */
    public void completeRun() {
        lastActiveTime = new Date();
        if (logger.isDebugEnabled()) {
            logger.debug("{} complete run, aggregateRootId: {}", getClass().getName(), aggregateRootId);
        }
        setAsNotRunning();
        if (getTotalUnHandledMessageCount() > 0) {
            tryRun();
        }
    }

    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - lastActiveTime.getTime()) >= timeoutSeconds;
    }

    private void processMessages() {
        ProcessingEvent message = processingEventQueue.poll();
        if (message != null) {
            lastActiveTime = new Date();
            try {
                handleProcessingEventAction.apply(message);
            } catch (Exception ex) {
                logger.error("{} run has unknown exception, aggregateRootId: {}", getClass().getName(), aggregateRootId, ex);
                Task.sleep(1);
                completeRun();
            }
        } else {
            completeRun();
        }
    }

    public String getAggregateRootTypeName() {
        return aggregateRootTypeName;
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public boolean tryUsing() {
        return isUsing.compareAndSet(0, 1);
    }

    public void exitUsing() {
        isUsing.set(0);
    }

    public void markAsRemoved() {
        isRemoved.set(1);
    }

    private void setAsRunning() {
        isRunning.set(1);
    }

    public boolean isRunning() {
        return isRunning.get() == 1;
    }

    public boolean isRemoved() {
        return isRemoved.get() == 1;
    }

    private void setAsNotRunning() {
        isRunning.set(0);
    }

    public int getWaitingMessageCount() {
        return waitingProcessingEventDict.size();
    }
}

