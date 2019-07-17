package com.enodeframework.infrastructure;

import com.enodeframework.common.function.Func1;
import com.enodeframework.common.io.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.enodeframework.common.io.Task.await;

public class DefaultMailBox<TMessage extends IMailBoxMessage, TMessageProcessResult> implements IMailBox<TMessage, TMessageProcessResult> {

    public Logger logger = LoggerFactory.getLogger(this.getClass());

    private String routingKey;
    private Date lastActiveTime;
    private boolean isRunning;
    private boolean isPauseRequested;
    private boolean isPaused;
    private long consumingSequence;
    private long consumedSequence;
    private final Object lockObj = new Object();
    private final Object asyncLock = new Object();
    private ConcurrentHashMap<Long, TMessage> messageDict;
    private Map<Long, TMessageProcessResult> requestToCompleteMessageDict;
    private Func1<TMessage, CompletableFuture> messageHandler;
    private Func1<List<TMessage>, CompletableFuture> messageListHandler;
    private boolean isBatchMessageProcess;
    private int batchSize;
    private long nextSequence;

    public DefaultMailBox(String routingKey, int batchSize, boolean isBatchMessageProcess, Func1<TMessage, CompletableFuture> messageHandler, Func1<List<TMessage>, CompletableFuture> messageListHandler) {
        messageDict = new ConcurrentHashMap<>();
        requestToCompleteMessageDict = new HashMap<>();
        this.batchSize = batchSize;
        this.routingKey = routingKey;
        this.isBatchMessageProcess = isBatchMessageProcess;
        this.messageHandler = messageHandler;
        this.messageListHandler = messageListHandler;
        consumedSequence = -1;
        lastActiveTime = new Date();
        if (isBatchMessageProcess && messageListHandler == null) {
            throw new NullPointerException("Parameter messageListHandler cannot be null");
        } else if (!isBatchMessageProcess && messageHandler == null) {
            throw new NullPointerException("Parameter messageHandler cannot be null");
        }
    }

    @Override
    public String getRoutingKey() {
        return this.routingKey;
    }

    @Override
    public Date getLastActiveTime() {
        return this.lastActiveTime;
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public boolean isPauseRequested() {
        return isPauseRequested;
    }

    @Override
    public boolean isPaused() {
        return isPaused;
    }

    @Override
    public long getConsumingSequence() {
        return consumingSequence;
    }

    @Override
    public long getConsumedSequence() {
        return consumedSequence;
    }

    @Override
    public long getMaxMessageSequence() {
        return nextSequence - 1;
    }

    @Override
    public long getTotalUnConsumedMessageCount() {
        return nextSequence - 1 - consumedSequence;
    }

    /**
     * 放入一个消息到MailBox，并自动尝试运行MailBox
     */
    @Override
    public void enqueueMessage(TMessage message) {
        synchronized (lockObj) {
            message.setSequence(nextSequence);
            message.setMailBox(this);
            // If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value.
            if (messageDict.putIfAbsent(message.getSequence(), message) == null) {
                nextSequence++;
                if (logger.isDebugEnabled()) {
                    logger.debug("{} enqueued new message, routingKey: {}, messageSequence: {}", getClass().getName(), routingKey, message.getSequence());

                }
                lastActiveTime = new Date();
                tryRun();
            }
        }
    }

    /**
     * 尝试运行一次MailBox，一次运行会处理一个消息或者一批消息，当前MailBox不能是运行中或者暂停中或者已暂停
     */
    @Override
    public void tryRun() {
        synchronized (lockObj) {
            if (isRunning || isPauseRequested || isPaused) {
                return;
            }
            setAsRunning();
            if (logger.isDebugEnabled()) {
                logger.debug("{} start run, routingKey: {}, consumingSequence: {}", getClass().getName(), routingKey, consumingSequence);
            }
            CompletableFuture.runAsync(this::processMessages);
        }
    }

    /**
     * 请求完成MailBox的单次运行，如果MailBox中还有剩余消息，则继续尝试运行下一次
     */
    @Override
    public void completeRun() {
        logger.debug("{} complete run, routingKey: {}", getClass().getName(), routingKey);
        setAsNotRunning();
        if (hasNextMessage()) {
            tryRun();
        }
    }

    /**
     * 暂停当前MailBox的运行，暂停成功可以确保当前MailBox不会处于运行状态，也就是不会在处理任何消息
     */
    @Override
    public void pause() {
        isPauseRequested = true;
        logger.debug("{} pause requested, routingKey: {}", getClass().getName(), routingKey);
        long count = 0L;
        while (isRunning) {
            Task.sleep(10);
            count++;
            if (count % 100 == 0) {
                logger.debug("{} pause requested, but wait for too long to stop the current mailbox, routingKey: {}, waitCount: {}", getClass().getName(), routingKey, count);
            }
        }
        isPaused = true;
    }

    /**
     * 恢复当前MailBox的运行，恢复后，当前MailBox又可以进行运行，需要手动调用TryRun方法来运行
     */
    @Override
    public void resume() {
        isPauseRequested = false;
        isPaused = false;
        logger.debug("{} resume requested, routingKey: {}, consumingSequence: {}", getClass().getName(), routingKey, consumingSequence);
    }

    @Override
    public void resetConsumingSequence(long consumingSequence) {
        lastActiveTime = new Date();
        this.consumingSequence = consumingSequence;
        requestToCompleteMessageDict.clear();
        logger.debug("{} reset consumingSequence, routingKey: {}, consumingSequence: {}", getClass().getName(), routingKey, consumingSequence);
    }

    @Override
    public void clear() {
        messageDict.clear();
        requestToCompleteMessageDict.clear();
        nextSequence = 0;
        consumingSequence = 0;
        consumedSequence = -1;
        lastActiveTime = new Date();
    }

    @Override
    public CompletableFuture<Void> completeMessage(TMessage message, TMessageProcessResult result) {
        synchronized (asyncLock) {
            lastActiveTime = new Date();
            try {
                if (message.getSequence() == consumedSequence + 1) {
                    messageDict.remove(message.getSequence());
                    await(completeMessageWithResult(message, result));
                    consumedSequence = processNextCompletedMessages(message.getSequence());
                } else if (message.getSequence() > consumedSequence + 1) {
                    requestToCompleteMessageDict.put(message.getSequence(), result);
                } else if (message.getSequence() < consumedSequence + 1) {
                    messageDict.remove(message.getSequence());
                    await(completeMessageWithResult(message, result));
                    requestToCompleteMessageDict.remove(message.getSequence());
                }
            } catch (Exception ex) {
                logger.error("MailBox complete message with result failed, routingKey: {}, message: {}, result: {}", routingKey, message, result, ex);
            }
        }
        return Task.completedTask;
    }

    @Override
    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - lastActiveTime.getTime()) >= timeoutSeconds;
    }

    protected CompletableFuture<Void> completeMessageWithResult(TMessage message, TMessageProcessResult result) {
        return Task.completedTask;
    }

    protected List<TMessage> filterMessages(List<TMessage> messages) {
        return messages;
    }

    private void processMessages() {
        lastActiveTime = new Date();
        try {
            if (isBatchMessageProcess) {
                long consumingSequence = this.consumingSequence;
                long scannedSequenceSize = 0;
                List<TMessage> messageList = new ArrayList<>();

                while (hasNextMessage(consumingSequence) && scannedSequenceSize < batchSize && !isPauseRequested) {
                    TMessage message = getMessage(consumingSequence);
                    if (message != null) {
                        messageList.add(message);
                    }
                    scannedSequenceSize++;
                    consumingSequence++;
                }

                List<TMessage> filterMessages = filterMessages(messageList);
                if (filterMessages != null && filterMessages.size() > 0) {
                    await(messageListHandler.apply(filterMessages));
                }
                this.consumingSequence = consumingSequence;

                if (filterMessages == null || filterMessages.size() == 0) {
                    completeRun();
                }
            } else {
                long scannedSequenceSize = 0;
                while (hasNextMessage() && scannedSequenceSize < batchSize && !isPauseRequested) {
                    TMessage message = getMessage(consumingSequence);
                    if (message != null) {
                        await(messageHandler.apply(message));
                    }
                    scannedSequenceSize++;
                    consumingSequence++;
                }
                completeRun();
            }
        } catch (Exception ex) {
            logger.error("MailBox run has unknown exception, mailboxType: {}, routingKey: {}", getClass().getName(), routingKey, ex);
            Task.sleep(1);
        }
    }

    private long processNextCompletedMessages(long baseSequence) {
        long returnSequence = baseSequence;
        long nextSequence = baseSequence + 1;
        while (requestToCompleteMessageDict.containsKey(nextSequence)) {
            TMessage message = messageDict.remove(nextSequence);
            if (message != null) {
                TMessageProcessResult result = requestToCompleteMessageDict.get(nextSequence);
                completeMessageWithResult(message, result);
            }
            requestToCompleteMessageDict.remove(nextSequence);
            returnSequence = nextSequence;
            nextSequence++;
        }
        return returnSequence;
    }

    private boolean hasNextMessage() {
        return consumingSequence < nextSequence;
    }

    private boolean hasNextMessage(long consumingSequence) {
        return consumingSequence < nextSequence;
    }

    private TMessage getMessage(long sequence) {
        return messageDict.getOrDefault(sequence, null);
    }

    private void setAsRunning() {
        isRunning = true;
    }

    private void setAsNotRunning() {
        isRunning = false;
    }

}
