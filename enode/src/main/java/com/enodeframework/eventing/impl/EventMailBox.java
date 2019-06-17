package com.enodeframework.eventing.impl;

import com.enodeframework.eventing.EventCommittingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author anruence@gmail.com
 */
public class EventMailBox {
    private static final Logger logger = LoggerFactory.getLogger(EventMailBox.class);

    private final String aggregateRootId;
    private final Queue<EventCommittingContext> messageQueue;
    private final Consumer<List<EventCommittingContext>> handleMessageAction;
    private AtomicBoolean isRunning;
    private int batchSize;
    private Date lastActiveTime;

    public EventMailBox(String aggregateRootId, int batchSize, Consumer<List<EventCommittingContext>> handleMessageAction) {
        this.aggregateRootId = aggregateRootId;
        this.batchSize = batchSize;
        this.handleMessageAction = handleMessageAction;
        this.messageQueue = new ConcurrentLinkedQueue<>();
        this.isRunning = new AtomicBoolean(false);
        this.lastActiveTime = new Date();
    }

    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public void enqueueMessage(EventCommittingContext message) {
        messageQueue.add(message);
        lastActiveTime = new Date();
        tryRun(false);
    }

    public void tryRun() {
        tryRun(false);
    }

    public void tryRun(boolean exitFirst) {
        if (exitFirst) {
            exit();
        }
        if (tryEnter()) {
            CompletableFuture.runAsync(this::run);
        }
    }

    public void run() {
        lastActiveTime = new Date();
        List<EventCommittingContext> contextList = null;
        try {
            EventCommittingContext context = null;

            while ((context = messageQueue.poll()) != null) {
                context.setEventMailBox(this);
                if (contextList == null) {
                    contextList = new ArrayList<>();
                }
                contextList.add(context);

                if (contextList.size() == batchSize) {
                    break;
                }
            }
            if (contextList != null && contextList.size() > 0) {
                handleMessageAction.accept(contextList);
            }
        } catch (Exception ex) {
            logger.error(String.format("Event mailbox run has unknown exception, aggregateRootId: %s", aggregateRootId), ex);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                //ignore
            }
        } finally {
            if (contextList == null || contextList.size() == 0) {
                exit();
                if (!messageQueue.isEmpty()) {
                    tryRun();
                }
            }
        }
    }

    public void exit() {
        isRunning.getAndSet(false);
    }

    public void clear() {
        messageQueue.clear();
    }

    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - lastActiveTime.getTime()) >= timeoutSeconds * 1000;
    }

    private boolean tryEnter() {
        return isRunning.compareAndSet(false, true);
    }

    public Date getLastActiveTime() {
        return this.lastActiveTime;
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
