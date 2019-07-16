package org.enodeframework.commanding;

import org.enodeframework.common.io.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author anruence@gmail.com
 */
public class ProcessingCommandMailbox {

    public static final Logger logger = LoggerFactory.getLogger(ProcessingCommandMailbox.class);
    private final Executor executor;
    private final Object lockObj = new Object();
    private final Object asyncLock = new Object();
    /**
     * Sequence 对应 ProcessingCommand
     */
    private final ConcurrentHashMap<Long, ProcessingCommand> messageDict;
    private final ConcurrentHashMap<String, Byte> duplicateCommandIdDict;
    private final IProcessingCommandHandler messageHandler;
    private final int batchSize;
    private final AtomicInteger isUsing = new AtomicInteger(0);
    private final AtomicInteger isRemoved = new AtomicInteger(0);
    private String aggregateRootId;
    private Date lastActiveTime;
    private boolean running;
    private boolean pauseRequested;
    private boolean paused;
    private long nextSequence;
    private long consumingSequence;

    public ProcessingCommandMailbox(String aggregateRootId, IProcessingCommandHandler messageHandler, int batchSize, Executor executor) {
        this.executor = executor;
        this.messageDict = new ConcurrentHashMap<>();
        this.duplicateCommandIdDict = new ConcurrentHashMap<>();
        this.messageHandler = messageHandler;
        this.batchSize = batchSize;
        this.aggregateRootId = aggregateRootId;
        lastActiveTime = new Date();
    }

    public Date getLastActiveTime() {
        return this.lastActiveTime;
    }

    public void setLastActiveTime(Date lastActiveTime) {
        this.lastActiveTime = lastActiveTime;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isPauseRequested() {
        return pauseRequested;
    }

    public boolean isPaused() {
        return paused;
    }

    public long getConsumingSequence() {
        return consumingSequence;
    }

    public long getMaxMessageSequence() {
        return nextSequence - 1;
    }

    public long getTotalUnHandledMessageCount() {
        return nextSequence - consumingSequence;
    }


    public String getAggregateRootId() {
        return aggregateRootId;
    }

    public void setAggregateRootId(String aggregateRootId) {
        this.aggregateRootId = aggregateRootId;
    }

    /**
     * 放入一个消息到MailBox，并自动尝试运行MailBox
     */
    public void enqueueMessage(ProcessingCommand message) {
        synchronized (lockObj) {
            message.setSequence(nextSequence);
            message.setMailBox(this);
            // If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value.
            if (messageDict.putIfAbsent(message.getSequence(), message) == null) {
                nextSequence++;
                if (logger.isDebugEnabled()) {
                    logger.debug("{} enqueued new message, aggregateRootId: {}, messageSequence: {}", getClass().getName(), aggregateRootId, message.getSequence());
                }
                lastActiveTime = new Date();
                tryRun();
            } else {
                logger.error("{} enqueue message failed, aggregateRootId: {}, messageId: {}, messageSequence: {}", getClass().getName(), aggregateRootId, message.getMessage().getId(), message.getSequence());
            }
        }
    }

    public void tryRun() {
        synchronized (lockObj) {
            if (isRunning() || isPauseRequested() || isPaused()) {
                return;
            }
            setAsRunning();
            if (logger.isDebugEnabled()) {
                logger.debug("{} start run, aggregateRootId: {}, consumingSequence: {}", getClass().getName(), aggregateRootId, consumingSequence);
            }
            CompletableFuture.supplyAsync(this::processMessages, executor);
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

    /**
     * 暂停当前MailBox的运行，暂停成功可以确保当前MailBox不会处于运行状态，也就是不会在处理任何消息
     */
    public void pause() {
        pauseRequested = true;
        if (logger.isDebugEnabled()) {
            logger.debug("{} pause requested, aggregateRootId: {}", getClass().getName(), aggregateRootId);
        }
        long count = 0L;
        while (running) {
            Task.sleep(10);
            count++;
            if (count % 100 == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("{} pause requested, but wait for too long to stop the current mailbox, aggregateRootId: {}, waitCount: {}", getClass().getName(), aggregateRootId, count);
                }
            }
        }
        lastActiveTime = new Date();
        paused = true;
    }

    /**
     * 恢复当前MailBox的运行，恢复后，当前MailBox又可以进行运行，需要手动调用TryRun方法来运行
     */
    public void resume() {
        pauseRequested = false;
        paused = false;
        lastActiveTime = new Date();
        if (logger.isDebugEnabled()) {
            logger.debug("{} resume requested, aggregateRootId: {}, consumingSequence: {}", getClass().getName(), aggregateRootId, consumingSequence);
        }
    }

    public void addDuplicateCommandId(String commandId) {
        duplicateCommandIdDict.putIfAbsent(commandId, (byte) 1);
    }

    public void resetConsumingSequence(long consumingSequence) {
        this.consumingSequence = consumingSequence;
        lastActiveTime = new Date();
        if (logger.isDebugEnabled()) {
            logger.debug("{} reset consumingSequence, aggregateRootId: {}, consumingSequence: {}", getClass().getName(), aggregateRootId, consumingSequence);
        }
    }

    public CompletableFuture<Void> completeMessage(ProcessingCommand message, CommandResult result) {
        try {
            ProcessingCommand removed = messageDict.remove(message.getSequence());
            if (removed != null) {
                duplicateCommandIdDict.remove(message.getMessage().getId());
                lastActiveTime = new Date();
                return message.completeAsync(result);
            }
        } catch (Exception ex) {
            logger.error("{} complete message with result failed, aggregateRootId: {}, messageId: {}, messageSequence: {}, result: {}", getClass().getName(), aggregateRootId, message.getMessage().getId(), message.getSequence(), result, ex);
        }
        return Task.completedTask;
    }

    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - lastActiveTime.getTime()) >= timeoutSeconds;
    }

    private CompletableFuture<Void> processMessages() {
        synchronized (asyncLock) {
            lastActiveTime = new Date();
            try {
                processMessagesRecursion(getTotalUnHandledMessageCount(), 0);
            } catch (Exception ex) {
                logger.error("{} run has unknown exception, aggregateRootId: {}", getClass().getName(), aggregateRootId, ex);
                Task.sleep(1);
            } finally {
                completeRun();
            }
        }
        return Task.completedTask;
    }

    public void processMessagesRecursion(long totalUnHandledMessageCount, long scannedCount) {
        if (!(totalUnHandledMessageCount > 0 && scannedCount < batchSize && !pauseRequested)) {
            return;
        }
        ProcessingCommand message = getMessage(consumingSequence);
        consumingSequence++;
        if (message != null) {
            if (duplicateCommandIdDict.containsKey(message.getMessage().getId())) {
                message.setDuplicated(true);
            }
            messageHandler.handleAsync(message).thenAccept(x -> {
                processMessagesRecursion(getTotalUnHandledMessageCount(), scannedCount + 1);
            });
            return;
        }
        processMessagesRecursion(getTotalUnHandledMessageCount(), scannedCount + 1);
    }


    private ProcessingCommand getMessage(long sequence) {
        return messageDict.getOrDefault(sequence, null);
    }

    private void setAsRunning() {
        running = true;
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

    public boolean isUsing() {
        return isUsing.get() == 1;
    }

    public boolean isRemoved() {
        return isRemoved.get() == 1;
    }

    private void setAsNotRunning() {
        running = false;
    }

}
