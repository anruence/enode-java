package com.enodeframework.eventing;

import com.enodeframework.common.function.Action1;
import com.enodeframework.common.io.Task;
import com.enodeframework.messaging.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

public class ProcessingDomainEventStreamMessageMailBox {
    private final static Logger logger = LoggerFactory.getLogger(ProcessingDomainEventStreamMessageMailBox.class);
    private final Object lockObj = new Object();
    private String aggregateRootId;
    private Date lastActiveTime;
    private boolean running;
    private int latestHandledEventVersion;
    private ConcurrentHashMap<Integer, ProcessingDomainEventStreamMessage> waitingMessageDict = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<ProcessingDomainEventStreamMessage> messageQueue;
    private Action1<ProcessingDomainEventStreamMessage> handleMessageAction;

    public ProcessingDomainEventStreamMessageMailBox(String aggregateRootId, int latestHandledEventVersion, Action1<ProcessingDomainEventStreamMessage> handleMessageAction) {
        messageQueue = new ConcurrentLinkedQueue<>();
        this.aggregateRootId = aggregateRootId;
        this.latestHandledEventVersion = latestHandledEventVersion;
        this.handleMessageAction = handleMessageAction;
        lastActiveTime = new Date();
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public boolean isRunning() {
        return running;
    }

    public long getTotalUnHandledMessageCount() {
        return messageQueue.size();
    }

    public void enqueueMessage(ProcessingDomainEventStreamMessage message) {
        synchronized (lockObj) {
            DomainEventStreamMessage eventStream = message.getMessage();
            if (eventStream.getVersion() == latestHandledEventVersion + 1) {
                message.setMailbox(this);
                messageQueue.add(message);
                if (logger.isDebugEnabled()) {
                    logger.debug("{} enqueued new message, aggregateRootType: {}, aggregateRootId: {}, commandId: {}, eventVersion: {}, eventStreamId: {}, eventTypes: {}, eventIds: {}",
                            getClass().getName(),
                            eventStream.getAggregateRootTypeName(),
                            eventStream.getAggregateRootId(),
                            eventStream.getCommandId(),
                            eventStream.getVersion(),
                            eventStream.getId(),
                            eventStream.getEvents().stream().map(x -> x.getClass().getName()).collect(Collectors.joining("|")),
                            eventStream.getEvents().stream().map(IMessage::getId).collect(Collectors.joining("|"))
                    );
                }
                latestHandledEventVersion = eventStream.getVersion();
                int nextVersion = eventStream.getVersion() + 1;
                while (waitingMessageDict.containsKey(nextVersion)) {
                    ProcessingDomainEventStreamMessage nextMessage = waitingMessageDict.remove(nextVersion);
                    DomainEventStreamMessage nextEventStream = nextMessage.getMessage();
                    nextMessage.setMailbox(this);
                    messageQueue.add(nextMessage);
                    latestHandledEventVersion = nextEventStream.getVersion();
                    if (logger.isDebugEnabled()) {
                        logger.debug("{} enqueued new message, aggregateRootType: {}, aggregateRootId: {}, commandId: {}, eventVersion: {}, eventStreamId: {}, eventTypes: {}, eventIds: {}",
                                getClass().getName(),
                                eventStream.getAggregateRootTypeName(),
                                nextEventStream.getAggregateRootId(),
                                nextEventStream.getCommandId(),
                                nextEventStream.getVersion(),
                                nextEventStream.getId(),
                                eventStream.getEvents().stream().map(x -> x.getClass().getName()).collect(Collectors.joining("|")),
                                eventStream.getEvents().stream().map(IMessage::getId).collect(Collectors.joining("|")));
                    }
                    nextVersion++;
                }
                lastActiveTime = new Date();
                tryRun();
            } else if (eventStream.getVersion() > latestHandledEventVersion + 1) {
                waitingMessageDict.put(eventStream.getVersion(), message);
            }
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
        ProcessingDomainEventStreamMessage message = messageQueue.poll();
        if (message != null) {
            lastActiveTime = new Date();
            try {
                handleMessageAction.apply(message);
            } catch (Exception ex) {
                logger.error("{} run has unknown exception, aggregateRootId: {}", getClass().getName(), aggregateRootId, ex);
                Task.sleep(1);
                completeRun();
            }
        } else {
            completeRun();
        }
    }

    private void setAsRunning() {
        running = true;
    }

    private void setAsNotRunning() {
        running = false;
    }

    public int getWaitingMessageCount() {
        return waitingMessageDict.size();
    }
}

