package com.enodeframework.infrastructure;


import com.enodeframework.common.function.Func1;
import com.enodeframework.common.io.Task;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static com.enodeframework.common.io.Task.await;

public class DefaultMailBox<TMessage extends IMailBoxMessage, TMessageProcessResult> implements IMailBox<TMessage, TMessageProcessResult> {

    public String RoutingKey;
    public Date LastActiveTime;
    public boolean IsRunning;
    public boolean IsPauseRequested;
    public boolean IsPaused;
    public long ConsumingSequence;
    public long ConsumedSequence;
    protected Logger _logger;
    private Object _lockObj = new Object();
    private Object _asyncLock = new Object();
    private ConcurrentHashMap<Long, TMessage> _messageDict;
    private Map<Long, TMessageProcessResult> _requestToCompleteMessageDict;
    private Func1<TMessage, CompletableFuture> _messageHandler;
    private Func1<List<TMessage>, CompletableFuture> _messageListHandler;
    private boolean _isBatchMessageProcess;
    private int _batchSize;
    private long _nextSequence;

    public DefaultMailBox(String routingKey, int batchSize, boolean isBatchMessageProcess, Func1<TMessage, CompletableFuture> messageHandler, Func1<List<TMessage>, CompletableFuture> messageListHandler) {
        _messageDict = new ConcurrentHashMap<>();
        _requestToCompleteMessageDict = new HashMap<>();
        _batchSize = batchSize;
        RoutingKey = routingKey;
        _isBatchMessageProcess = isBatchMessageProcess;
        _messageHandler = messageHandler;
        _messageListHandler = messageListHandler;
        ConsumedSequence = -1;
        LastActiveTime = new Date();
        if (isBatchMessageProcess && messageListHandler == null) {
            throw new NullPointerException("Parameter messageListHandler cannot be null");
        } else if (!isBatchMessageProcess && messageHandler == null) {
            throw new NullPointerException("Parameter messageHandler cannot be null");
        }
    }

    @Override
    public String getRoutingKey() {
        return this.RoutingKey;
    }

    @Override
    public Date getLastActiveTime() {
        return this.LastActiveTime;
    }

    @Override
    public boolean isRunning() {
        return IsRunning;
    }

    @Override
    public boolean isPauseRequested() {
        return IsPauseRequested;
    }

    @Override
    public boolean isPaused() {
        return IsPaused;
    }

    @Override
    public long getConsumingSequence() {
        return ConsumingSequence;
    }

    @Override
    public long getConsumedSequence() {
        return ConsumedSequence;
    }

    @Override
    public long getMaxMessageSequence() {
        return _nextSequence - 1;
    }

    @Override
    public long getTotalUnConsumedMessageCount() {
        return _nextSequence - 1 - ConsumedSequence;
    }

    /**
     * 放入一个消息到MailBox，并自动尝试运行MailBox
     */
    @Override
    public void enqueueMessage(TMessage message) {
        synchronized (_lockObj) {
            message.setSequence(_nextSequence);
            message.setMailBox(this);
            // If the specified key is not already associated with a value (or is mapped to null) associates it with the given value and returns null, else returns the current value.
            if (_messageDict.putIfAbsent(message.getSequence(), message) == null) {
                _nextSequence++;
                if (_logger.isDebugEnabled()) {
                    _logger.debug("{} enqueued new message, routingKey: {}, messageSequence: {}", getClass().getName(), RoutingKey, message.getSequence());

                }
                LastActiveTime = new Date();
                tryRun();
            }
        }
    }

    /**
     * 尝试运行一次MailBox，一次运行会处理一个消息或者一批消息，当前MailBox不能是运行中或者暂停中或者已暂停
     */
    @Override
    public void tryRun() {
        synchronized (_lockObj) {
            if (IsRunning || IsPauseRequested || IsPaused) {
                return;
            }
            SetAsRunning();
            if (_logger.isDebugEnabled()) {
                _logger.debug("{} start run, routingKey: {}, consumingSequence: {}", getClass().getName(), RoutingKey, ConsumingSequence);
            }
            CompletableFuture.runAsync(() -> ProcessMessages());
        }
    }

    /**
     * 请求完成MailBox的单次运行，如果MailBox中还有剩余消息，则继续尝试运行下一次
     */
    @Override
    public void completeRun() {
        _logger.debug("{} complete run, routingKey: {}", getClass().getName(), RoutingKey);
        SetAsNotRunning();
        if (HasNextMessage()) {
            tryRun();
        }
    }

    /**
     * 暂停当前MailBox的运行，暂停成功可以确保当前MailBox不会处于运行状态，也就是不会在处理任何消息
     */
    @Override
    public void pause() {
        IsPauseRequested = true;
        _logger.debug("{} pause requested, routingKey: {}", getClass().getName(), RoutingKey);
        long count = 0L;
        while (IsRunning) {
            com.enodeframework.common.io.Task.sleep(10);
            count++;
            if (count % 100 == 0) {
                _logger.debug("{} pause requested, but wait for too long to stop the current mailbox, routingKey: {}, waitCount: {}", getClass().getName(), RoutingKey, count);
            }
        }
        IsPaused = true;
    }

    /**
     * 恢复当前MailBox的运行，恢复后，当前MailBox又可以进行运行，需要手动调用TryRun方法来运行
     */
    @Override
    public void resume() {
        IsPauseRequested = false;
        IsPaused = false;
        _logger.debug("{} resume requested, routingKey: {}, consumingSequence: {}", getClass().getName(), RoutingKey, ConsumingSequence);
    }

    @Override
    public void resetConsumingSequence(long consumingSequence) {
        LastActiveTime = new Date();
        ConsumingSequence = consumingSequence;
        _requestToCompleteMessageDict.clear();
        _logger.debug("{} reset consumingSequence, routingKey: {}, consumingSequence: {}", getClass().getName(), RoutingKey, consumingSequence);
    }

    @Override
    public void clear() {
        _messageDict.clear();
        _requestToCompleteMessageDict.clear();
        _nextSequence = 0;
        ConsumingSequence = 0;
        ConsumedSequence = -1;
        LastActiveTime = new Date();
    }

    @Override
    public CompletableFuture completeMessage(TMessage message, TMessageProcessResult result) {
        synchronized (_asyncLock) {
            LastActiveTime = new Date();
            try {
                if (message.getSequence() == ConsumedSequence + 1) {
                    _messageDict.remove(message.getSequence());
                    await(completeMessageWithResult(message, result));
                    ConsumedSequence = ProcessNextCompletedMessages(message.getSequence());
                } else if (message.getSequence() > ConsumedSequence + 1) {
                    _requestToCompleteMessageDict.put(message.getSequence(), result);
                } else if (message.getSequence() < ConsumedSequence + 1) {
                    _messageDict.remove(message.getSequence());
                    await(completeMessageWithResult(message, result));
                    _requestToCompleteMessageDict.remove(message.getSequence());
                }
            } catch (Exception ex) {
                _logger.error("MailBox complete message with result failed, routingKey: {}, message: {}, result: {}", RoutingKey, message, result, ex);
            }
        }
        return Task.completedTask;
    }

    @Override
    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - LastActiveTime.getTime()) >= timeoutSeconds;
    }

    protected CompletableFuture<Void> completeMessageWithResult(TMessage message, TMessageProcessResult result) {
        return Task.completedTask;
    }

    protected List<TMessage> FilterMessages(List<TMessage> messages) {
        return messages;
    }

    private void ProcessMessages() {
        LastActiveTime = new Date();
        try {
            if (_isBatchMessageProcess) {
                long consumingSequence = ConsumingSequence;
                long scannedSequenceSize = 0;
                List<TMessage> messageList = new ArrayList<>();

                while (HasNextMessage(consumingSequence) && scannedSequenceSize < _batchSize && !IsPauseRequested) {
                    TMessage message = GetMessage(consumingSequence);
                    if (message != null) {
                        messageList.add(message);
                    }
                    scannedSequenceSize++;
                    consumingSequence++;
                }

                List<TMessage> filterMessages = FilterMessages(messageList);
                if (filterMessages != null && filterMessages.size() > 0) {
                    await(_messageListHandler.apply(filterMessages));
                }
                ConsumingSequence = consumingSequence;

                if (filterMessages == null || filterMessages.size() == 0) {
                    completeRun();
                }
            } else {
                long scannedSequenceSize = 0;
                while (HasNextMessage() && scannedSequenceSize < _batchSize && !IsPauseRequested) {
                    TMessage message = GetMessage(ConsumingSequence);
                    if (message != null) {
                        await(_messageHandler.apply(message));
                    }
                    scannedSequenceSize++;
                    ConsumingSequence++;
                }
                completeRun();
            }
        } catch (Exception ex) {
            _logger.error("MailBox run has unknown exception, mailboxType: {}, routingKey: {}", getClass().getName(), RoutingKey, ex);
            Task.sleep(1);
        }
    }

    private long ProcessNextCompletedMessages(long baseSequence) {
        long returnSequence = baseSequence;
        long nextSequence = baseSequence + 1;
        while (_requestToCompleteMessageDict.containsKey(nextSequence)) {
            TMessage message = _messageDict.remove(nextSequence);
            if (message != null) {
                TMessageProcessResult result = _requestToCompleteMessageDict.get(nextSequence);
                completeMessageWithResult(message, result);
            }
            _requestToCompleteMessageDict.remove(nextSequence);
            returnSequence = nextSequence;
            nextSequence++;
        }
        return returnSequence;
    }

    private boolean HasNextMessage() {
        return ConsumingSequence < _nextSequence;
    }

    private boolean HasNextMessage(long consumingSequence) {
        return consumingSequence < _nextSequence;
    }

    private TMessage GetMessage(long sequence) {
        return _messageDict.getOrDefault(sequence, null);
    }

    private void SetAsRunning() {
        IsRunning = true;
    }

    private void SetAsNotRunning() {
        IsRunning = false;
    }

}
