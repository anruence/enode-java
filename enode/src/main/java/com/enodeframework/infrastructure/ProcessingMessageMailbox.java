package com.enodeframework.infrastructure;

import com.enodeframework.common.io.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.enodeframework.common.io.Task.await;

/**
 * @author anruence@gmail.com
 */
public class ProcessingMessageMailbox<X extends IProcessingMessage<X, Y>, Y extends IMessage> {
    private static final Logger logger = LoggerFactory.getLogger(ProcessingMessageMailbox.class);
    private final ConcurrentLinkedQueue<X> messageQueue;
    private final IProcessingMessageScheduler<X, Y> scheduler;
    private final IProcessingMessageHandler<X, Y> messageHandler;
    private final Object lockObj = new Object();
    private String routingKey;
    private ConcurrentMap<Integer, X> waitingMessageDict;
    private AtomicBoolean isRunning = new AtomicBoolean(false);
    private Date lastActiveTime;

    public ProcessingMessageMailbox(String routingKey, IProcessingMessageScheduler<X, Y> scheduler, IProcessingMessageHandler<X, Y> messageHandler) {
        this.routingKey = routingKey;
        this.scheduler = scheduler;
        this.messageHandler = messageHandler;
        messageQueue = new ConcurrentLinkedQueue<>();
        lastActiveTime = new Date();
    }

    public void enqueueMessage(X processingMessage) {
        processingMessage.setMailbox(this);
        messageQueue.add(processingMessage);
        lastActiveTime = new Date();
        tryRun();
    }

    public void addWaitingForRetryMessage(X waitingMessage) {
        if (!(waitingMessage.getMessage() instanceof ISequenceMessage)) {
            throw new IllegalArgumentException("sequenceMessage should not be null.");
        }
        ISequenceMessage sequenceMessage = (ISequenceMessage) waitingMessage.getMessage();
        if (waitingMessageDict == null) {
            synchronized (lockObj) {
                if (waitingMessageDict == null) {
                    waitingMessageDict = new ConcurrentHashMap<>();
                }
            }
        }
        waitingMessageDict.putIfAbsent(sequenceMessage.getVersion(), waitingMessage);
        lastActiveTime = new Date();
        exit();
        tryRun();
    }

    public void completeMessage(X processingMessage) {
        lastActiveTime = new Date();
        if (!tryExecuteWaitingMessage(processingMessage)) {
            exit();
            tryRun();
        }
    }

    public void run() {
        lastActiveTime = new Date();
        X processingMessage = null;
        try {
            processingMessage = messageQueue.poll();
            if (processingMessage != null) {
                await(messageHandler.handleAsync(processingMessage));
            }
        } catch (Exception ex) {
            logger.error(String.format("Message mailbox run has unknown exception, routingKey: %s, commandId: %s", routingKey, processingMessage != null ? processingMessage.getMessage().getId() : ""), ex);
            Task.sleep(1);
        } finally {
            if (processingMessage == null) {
                exit();
                if (!messageQueue.isEmpty()) {
                    tryRun();
                }
            }
        }
    }

    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - lastActiveTime.getTime()) >= timeoutSeconds * 1000L;
    }

    private boolean tryExecuteWaitingMessage(X currentCompletedMessage) {
        if (!(currentCompletedMessage.getMessage() instanceof ISequenceMessage)) {
            return false;
        }
        ISequenceMessage sequenceMessage = (ISequenceMessage) currentCompletedMessage.getMessage();
        if (sequenceMessage == null) {
            return false;
        }
        if (waitingMessageDict == null) {
            return false;
        }
        X nextMessage = waitingMessageDict.remove(sequenceMessage.getVersion() + 1);
        if (nextMessage != null) {
            scheduler.scheduleMessage(nextMessage);
            return true;
        }
        return false;
    }

    private void tryRun() {
        if (tryEnter()) {
            scheduler.scheduleMailbox(this);
        }
    }

    public boolean tryEnter() {
        return isRunning.compareAndSet(false, true);
    }

    public void exit() {
        isRunning.getAndSet(false);
    }

    public Date getLastActiveTime() {
        return lastActiveTime;
    }

    public boolean isRunning() {
        return isRunning.get();
    }
}
