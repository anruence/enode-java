package com.qianzhui.enode.eventing.impl;

import com.qianzhui.enode.common.logging.ENodeLogger;
import com.qianzhui.enode.eventing.EventCommittingContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class EventMailBox {
    private static final Logger _logger = ENodeLogger.getLog();

    private final String _aggregateRootId;
    private final Queue<EventCommittingContext> _messageQueue;
    private final Consumer<List<EventCommittingContext>> _handleMessageAction;
    private AtomicBoolean _isRunning;
    private int _batchSize;
    private Date _lastActiveTime;

    public String getAggregateRootId() {
        return _aggregateRootId;
    }

    public EventMailBox(String aggregateRootId, int batchSize, Consumer<List<EventCommittingContext>> handleMessageAction) {
        _aggregateRootId = aggregateRootId;
        _messageQueue = new ConcurrentLinkedQueue<>();
        _batchSize = batchSize;
        _handleMessageAction = handleMessageAction;
        _isRunning = new AtomicBoolean(false);
        _lastActiveTime = new Date();
    }

    public void enqueueMessage(EventCommittingContext message) {
        _messageQueue.add(message);
        _lastActiveTime = new Date();
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
        _lastActiveTime = new Date();
        List<EventCommittingContext> contextList = null;
        try {
            EventCommittingContext context = null;

            while ((context = _messageQueue.poll()) != null) {
                context.setEventMailBox(this);
                if (contextList == null) {
                    contextList = new ArrayList<>();
                }
                contextList.add(context);

                if (contextList.size() == _batchSize) {
                    break;
                }
            }
            if (contextList != null && contextList.size() > 0) {
                _handleMessageAction.accept(contextList);
            }
        } catch (Exception ex) {
            _logger.error(String.format("Event mailbox run has unknown exception, aggregateRootId: %s", _aggregateRootId), ex);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                //ignore
            }
        } finally {
            if (contextList == null || contextList.size() == 0) {
                exit();
                if (!_messageQueue.isEmpty()) {
                    tryRun();
                }
            }
        }
    }

    public void exit() {
        _isRunning.getAndSet(false);
    }


    public void clear() {
        _messageQueue.clear();
    }

    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - _lastActiveTime.getTime()) >= timeoutSeconds * 1000;
    }

    private boolean tryEnter() {
        return _isRunning.compareAndSet(false, true);
    }

    public Date getLastActiveTime() {
        return _lastActiveTime;
    }

    public boolean isRunning() {
        return _isRunning.get();
    }
}
