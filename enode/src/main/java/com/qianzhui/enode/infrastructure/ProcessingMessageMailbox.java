package com.qianzhui.enode.infrastructure;

import com.qianzhui.enode.common.logging.ENodeLogger;
import org.slf4j.Logger;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessingMessageMailbox<X extends IProcessingMessage<X, Y>, Y extends IMessage> {
    private static final Logger _logger = ENodeLogger.getLog();

    private final String _routingKey;
    private ConcurrentMap<Integer, X> _waitingMessageDict;
    private final ConcurrentLinkedQueue<X> _messageQueue;
    private final IProcessingMessageScheduler<X, Y> _scheduler;
    private final IProcessingMessageHandler<X, Y> _messageHandler;
    private AtomicBoolean _isRunning = new AtomicBoolean(false);
    private final Object _lockObj = new Object();
    private Date _lastActiveTime;

    public ProcessingMessageMailbox(String routingKey, IProcessingMessageScheduler<X, Y> scheduler, IProcessingMessageHandler<X, Y> messageHandler) {
        _routingKey = routingKey;
        _messageQueue = new ConcurrentLinkedQueue<>();
        _scheduler = scheduler;
        _messageHandler = messageHandler;
        _lastActiveTime = new Date();
    }

    public void enqueueMessage(X processingMessage) {
        processingMessage.setMailbox(this);
        _messageQueue.add(processingMessage);
        _lastActiveTime = new Date();
        tryRun();
    }

    public void addWaitingForRetryMessage(X waitingMessage) {
        if (!(waitingMessage.getMessage() instanceof ISequenceMessage)) {
            throw new IllegalArgumentException("sequenceMessage should not be null.");
        }

        ISequenceMessage sequenceMessage = (ISequenceMessage) waitingMessage.getMessage();

        if (_waitingMessageDict == null) {
            synchronized (_lockObj) {
                if (_waitingMessageDict == null) {
                    _waitingMessageDict = new ConcurrentHashMap<>();
                }
            }
        }

        _waitingMessageDict.putIfAbsent(sequenceMessage.version(), waitingMessage);

        _lastActiveTime = new Date();
        exit();
        tryRun();
    }

    public void completeMessage(X processingMessage) {
        _lastActiveTime = new Date();
        if (!tryExecuteWaitingMessage(processingMessage)) {
            exit();
            tryRun();
        }
    }

    public void run() {
        _lastActiveTime = new Date();
        X processingMessage = null;
        try {
            processingMessage = _messageQueue.poll();

            if (processingMessage != null) {
                _messageHandler.handleAsync(processingMessage);
            }
        } catch (Exception ex) {
            _logger.error(String.format("Message mailbox run has unknown exception, routingKey: %s, commandId: %s", _routingKey, processingMessage != null ? processingMessage.getMessage().id() : ""), ex);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            if (processingMessage == null) {
                exit();
                if (!_messageQueue.isEmpty()) {
                    tryRun();
                }
            }
        }
    }

    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - _lastActiveTime.getTime()) >= timeoutSeconds * 1000L;
    }

    private boolean tryExecuteWaitingMessage(X currentCompletedMessage) {
        if (!(currentCompletedMessage.getMessage() instanceof ISequenceMessage)) {
            return false;
        }

        ISequenceMessage sequenceMessage = (ISequenceMessage) currentCompletedMessage.getMessage();
        if (sequenceMessage == null) {
            return false;
        }

        if (_waitingMessageDict == null) {
            return false;
        }

        X nextMessage = _waitingMessageDict.remove(sequenceMessage.version() + 1);

        if (nextMessage != null) {
            _scheduler.scheduleMessage(nextMessage);
            return true;
        }
        return false;
    }

    private void tryRun() {
        if (tryEnter()) {
            _scheduler.scheduleMailbox(this);
        }
    }

    public boolean tryEnter() {
        return _isRunning.compareAndSet(false, true);
    }

    public void exit() {
        _isRunning.getAndSet(false);
    }

    public Date getLastActiveTime() {
        return _lastActiveTime;
    }

    public boolean isRunning() {
        return _isRunning.get();
    }
}
