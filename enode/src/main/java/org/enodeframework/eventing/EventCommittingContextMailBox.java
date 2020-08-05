package org.enodeframework.eventing;

import org.enodeframework.common.function.Action1;
import org.enodeframework.common.io.Task;
import org.enodeframework.messaging.IMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class EventCommittingContextMailBox {

    public final static Logger logger = LoggerFactory.getLogger(EventCommittingContextMailBox.class);
    private final static Byte ONE_BYTE = 1;
    private final Object lockObj = new Object();
    private final Executor executor;
    private final Object processMessageLockObj = new Object();
    private Date lastActiveTime;
    private boolean running;
    private final int number;
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Byte>> aggregateDictDict;
    private final ConcurrentLinkedQueue<EventCommittingContext> messageQueue;
    private final Action1<List<EventCommittingContext>> handleMessageAction;
    private final int batchSize;

    public EventCommittingContextMailBox(int number, int batchSize, Action1<List<EventCommittingContext>> handleMessageAction, Executor executor) {
        this.executor = executor;
        this.aggregateDictDict = new ConcurrentHashMap<>();
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.handleMessageAction = handleMessageAction;
        this.number = number;
        this.batchSize = batchSize;
        this.lastActiveTime = new Date();
    }

    public Date getLastActiveTime() {
        return this.lastActiveTime;
    }

    public boolean isRunning() {
        return running;
    }

    public long getTotalUnHandledMessageCount() {
        return messageQueue.size();
    }

    /**
     * 放入一个消息到MailBox，并自动尝试运行MailBox
     */
    public void enqueueMessage(EventCommittingContext message) {
        synchronized (lockObj) {
            ConcurrentHashMap<String, Byte> eventDict = aggregateDictDict.computeIfAbsent(message.getEventStream().getAggregateRootId(), x -> new ConcurrentHashMap<>());
            // If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value.
            if (eventDict.putIfAbsent(message.getEventStream().getId(), ONE_BYTE) == null) {
                message.setMailBox(this);
                messageQueue.add(message);
                if (logger.isDebugEnabled()) {
                    logger.debug("{} enqueued new message, mailboxNumber: {}, aggregateRootId: {}, commandId: {}, eventVersion: {}, eventStreamId: {}, eventIds: {}",
                            getClass().getName(),
                            number,
                            message.getEventStream().getAggregateRootId(),
                            message.getProcessingCommand().getMessage().getId(),
                            message.getEventStream().getVersion(),
                            message.getEventStream().getId(),
                            message.getEventStream().getEvents().stream().map(IMessage::getId).collect(Collectors.joining("|"))
                    );
                }
                lastActiveTime = new Date();
                tryRun();
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
                logger.debug("{} start run, mailboxNumber: {}", getClass().getName(), number);
            }
            CompletableFuture.runAsync(this::processMessages, executor);
        }
    }

    /**
     * 请求完成MailBox的单次运行，如果MailBox中还有剩余消息，则继续尝试运行下一次
     */
    public void completeRun() {
        lastActiveTime = new Date();
        if (logger.isDebugEnabled()) {
            logger.debug("{} complete run, mailboxNumber: {}", getClass().getName(), number);
        }
        setAsNotRunning();
        if (getTotalUnHandledMessageCount() > 0) {
            tryRun();
        }
    }

    public void removeAggregateAllEventCommittingContexts(String aggregateRootId) {
        aggregateDictDict.remove(aggregateRootId);
    }

    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - lastActiveTime.getTime()) >= timeoutSeconds;
    }

    private void processMessages() {
        synchronized (processMessageLockObj) {
            lastActiveTime = new Date();
            List<EventCommittingContext> messageList = new ArrayList<>();
            while (messageList.size() < batchSize) {
                EventCommittingContext message = messageQueue.poll();
                if (message != null) {
                    ConcurrentHashMap<String, Byte> eventDict = aggregateDictDict.getOrDefault(message.getEventStream().getAggregateRootId(), null);
                    if (eventDict != null) {
                        if (eventDict.remove(message.getEventStream().getId()) != null) {
                            messageList.add(message);
                        }
                    }
                } else {
                    break;
                }
            }
            if (messageList.size() == 0) {
                completeRun();
                return;
            }
            try {
                handleMessageAction.apply(messageList);
            } catch (Exception ex) {
                logger.error("{} run has unknown exception, mailboxNumber: {}", getClass().getName(), number, ex);
                Task.sleep(1);
                completeRun();
            }
        }
    }

    private void setAsRunning() {
        running = true;
    }

    private void setAsNotRunning() {
        running = false;
    }

    public int getNumber() {
        return number;
    }
}

