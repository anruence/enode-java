package org.enodeframework.eventing;

import org.enodeframework.common.exception.ENodeRuntimeException;
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

    private String aggregateRootId;

    private String aggregateRootTypeName;

    private Date lastActiveTime;

    private int nextExpectingEventVersion = 1;

    private AtomicInteger isUsing = new AtomicInteger(0);
    private AtomicInteger isRemoved = new AtomicInteger(0);
    private AtomicInteger isRunning = new AtomicInteger(0);

    private ConcurrentHashMap<Integer, ProcessingEvent> waitingProcessingEventDict = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<ProcessingEvent> processingEventQueue;
    private Action1<ProcessingEvent> handleProcessingEventAction;

    public ProcessingEventMailBox(String aggregateRootTypeName, String aggregateRootId, Action1<ProcessingEvent> handleProcessingEventAction) {
        processingEventQueue = new ConcurrentLinkedQueue<>();
        this.aggregateRootId = aggregateRootId;
        this.aggregateRootTypeName = aggregateRootTypeName;
        this.handleProcessingEventAction = handleProcessingEventAction;
        lastActiveTime = new Date();
    }

    private void tryEnqueueWaitingMessage() {
        while (waitingProcessingEventDict.containsKey(nextExpectingEventVersion)) {
            ProcessingEvent nextProcessingEvent = waitingProcessingEventDict.remove(nextExpectingEventVersion);
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
            if (version > this.nextExpectingEventVersion) {
                this.nextExpectingEventVersion = version;
                logger.info("{} refreshed nextExpectingEventVersion, aggregateRootId: {}, aggregateRootTypeName: {}, version: {}", getClass().getName(), aggregateRootId, aggregateRootTypeName, nextExpectingEventVersion);
                tryEnqueueWaitingMessage();
                lastActiveTime = new Date();
                tryRun();
            } else {
                logger.info("{} nextExpectingEventVersion ignored, aggregateRootId: {}, aggregateRootTypeName: {}, nextExpectingEventVersion: {}, current _nextExpectingEventVersion: {}", getClass().getName(), aggregateRootId, aggregateRootTypeName, nextExpectingEventVersion, nextExpectingEventVersion);
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
                        String.join("|", processingEvent.getMessage().getEvents().stream().map(x -> x.getClass().getName()).collect(Collectors.toList())),
                        String.join("|", processingEvent.getMessage().getEvents().stream().map(x -> x.getId()).collect(Collectors.toList()))
                );
            }
        }
    }

    public EnqueueMessageResult enqueueMessage(ProcessingEvent processingEvent) {
        synchronized (lockObj) {
            if (isRemoved()) {
                throw new ENodeRuntimeException(String.format("ProcessingEventMailBox was removed, cannot allow to enqueue message, aggregateRootTypeName: %s, aggregateRootId: %s", aggregateRootTypeName, aggregateRootId));
            }
            DomainEventStreamMessage eventStream = processingEvent.getMessage();
            if (eventStream.getVersion() == nextExpectingEventVersion) {
                enqueueEventStream(processingEvent);
                tryEnqueueWaitingMessage();
                lastActiveTime = new Date();
                tryRun();
                return EnqueueMessageResult.Success;
            } else if (eventStream.getVersion() > nextExpectingEventVersion) {
                if (waitingProcessingEventDict.putIfAbsent(eventStream.getVersion(), processingEvent) == null) {
                    logger.warn("{} later version of message arrived, added it to the waiting list, aggregateRootType: {}, aggregateRootId: {}, commandId: {}, eventVersion: {}, eventStreamId: {}, eventTypes: {}, eventIds: {}, _nextExpectingEventVersion: {}",
                            getClass().getName(),
                            eventStream.getAggregateRootTypeName(),
                            eventStream.getAggregateRootId(),
                            eventStream.getCommandId(),
                            eventStream.getVersion(),
                            eventStream.getId(),
                            eventStream.getEvents().stream().map(x -> x.getClass().getName()).collect(Collectors.joining("|")),
                            eventStream.getEvents().stream().map(IMessage::getId).collect(Collectors.joining("|")),
                            nextExpectingEventVersion
                    );
                }
                return EnqueueMessageResult.AddToWaitingList;
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

