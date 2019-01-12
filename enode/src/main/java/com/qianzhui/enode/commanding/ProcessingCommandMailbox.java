package com.qianzhui.enode.commanding;

import com.qianzhui.enode.ENode;
import com.qianzhui.enode.common.logging.ENodeLogger;
import com.qianzhui.enode.common.threading.ManualResetEvent;
import org.slf4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProcessingCommandMailbox {
    private static final Logger _logger = ENodeLogger.getLog();

    private final Object _lockObj = new Object();
    private final Object _lockObj2 = new Object();
    private final String _aggregateRootId;
    private final ConcurrentMap<Long, ProcessingCommand> _messageDict;
    private final Map<Long, CommandResult> _requestToCompleteCommandDict;
    private final IProcessingCommandHandler _messageHandler;
    private final ManualResetEvent _pauseWaitHandle;
    private final ManualResetEvent _processingWaitHandle;
    private final int _batchSize;
    private long _nextSequence;
    private long _consumingSequence;
    private long _consumedSequence;
    private AtomicBoolean _isRunning;
    private volatile boolean _isProcessingCommand;
    //已终止命令处理
    private volatile boolean _isPaused;
    //命令终止中标志
    private volatile boolean _inPausing;
    private Date _lastActiveTime;

    public String getAggregateRootId() {
        return _aggregateRootId;
    }

    public ProcessingCommandMailbox(String aggregateRootId, IProcessingCommandHandler messageHandler) {
        _messageDict = new ConcurrentHashMap<>();
        _requestToCompleteCommandDict = new HashMap<>();
        _pauseWaitHandle = new ManualResetEvent(false);
        _processingWaitHandle = new ManualResetEvent(false);
        _batchSize = ENode.getInstance().getSetting().getCommandMailBoxPersistenceMaxBatchSize();
        _aggregateRootId = aggregateRootId;
        _messageHandler = messageHandler;
        _consumedSequence = -1;
        _isRunning = new AtomicBoolean(false);
        _lastActiveTime = new Date();
    }

    public void enqueueMessage(ProcessingCommand message) {
        //TODO synchronized
        synchronized (_lockObj) {
            message.setSequence(_nextSequence);
            message.setMailbox(this);
            ProcessingCommand processingCommand = _messageDict.putIfAbsent(message.getSequence(), message);
            if (processingCommand == null) {
                _nextSequence++;
            }
        }
        _lastActiveTime = new Date();
        tryRun();
    }

    public void pause() {
        _lastActiveTime = new Date();
        _inPausing = true;
        _pauseWaitHandle.reset();
        while (_isProcessingCommand) {
            _logger.info("Request to pause the command mailbox, but the mailbox is currently processing command, so we should wait for a while, aggregateRootId: {}", _aggregateRootId);
            _processingWaitHandle.waitOne(1000);
        }
        _isPaused = true;
    }

    public void resume() {
        _lastActiveTime = new Date();
        _isPaused = false;
        _pauseWaitHandle.set();
        tryRun();
    }

    public void resetConsumingSequence(long consumingSequence) {
        _lastActiveTime = new Date();
        _consumingSequence = consumingSequence;
        _requestToCompleteCommandDict.clear();
    }

    //TODO async
    public CompletableFuture completeMessage(ProcessingCommand processingCommand, CommandResult commandResult) {
        //TODO synchronized
        synchronized (_lockObj2) {
            _lastActiveTime = new Date();
            try {
                if (processingCommand.getSequence() == _consumedSequence + 1) {
                    _messageDict.remove(processingCommand.getSequence());
                    completeCommand(processingCommand, commandResult);
                    _consumedSequence = processNextCompletedCommands(processingCommand.getSequence());
                } else if (processingCommand.getSequence() > _consumedSequence + 1) {
                    _requestToCompleteCommandDict.put(processingCommand.getSequence(), commandResult);
                } else if (processingCommand.getSequence() < _consumedSequence + 1) {
                    _messageDict.remove(processingCommand.getSequence());
                    completeCommand(processingCommand, commandResult);
                    _requestToCompleteCommandDict.remove(processingCommand.getSequence());
                }
            } catch (Exception ex) {
                _logger.error(String.format("Command mailbox complete command failed, commandId: %s, aggregateRootId: %s", processingCommand.getMessage().id(), processingCommand.getMessage().getAggregateRootId()), ex);
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    public void run() {
        _lastActiveTime = new Date();
        while (_isPaused) {
            _logger.info("Command mailbox is pausing and we should wait for a while, aggregateRootId: {}", _aggregateRootId);
            _pauseWaitHandle.waitOne(1000);
        }

        ProcessingCommand processingCommand = null;

        try {
            _processingWaitHandle.reset();
            _isProcessingCommand = true;
            int count = 0;

            while (_consumingSequence < _nextSequence && count < _batchSize) {
                if (_inPausing) {
                    _logger.info("Command mailbox is pausing and we should exit batch loop, aggregateRootId: {}", _aggregateRootId);
                    break;
                }
                processingCommand = getProcessingCommand(_consumingSequence);

                if (processingCommand != null) {
                    _messageHandler.handle(processingCommand);
                }
                _consumingSequence++;
                count++;
            }
        } catch (Throwable ex) {
            _logger.error(String.format("Command mailbox run has unknown exception, aggregateRootId: %s, commandId: %s", _aggregateRootId, processingCommand != null ? processingCommand.getMessage().id() : ""), ex);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                //ignore
                e.printStackTrace();
            }
        } finally {
            _isProcessingCommand = false;
            _processingWaitHandle.set();
            exit();
            if (_consumingSequence < _nextSequence) {
                tryRun();
            }
        }
    }

    public boolean isInactive(int timeoutSeconds) {
        return (System.currentTimeMillis() - _lastActiveTime.getTime()) >= timeoutSeconds * 1000L;
    }

    private ProcessingCommand getProcessingCommand(long sequence) {
        return _messageDict.get(sequence);
    }

    private long processNextCompletedCommands(long baseSequence) {
        long returnSequence = baseSequence;
        long nextSequence = baseSequence + 1;

        while (_requestToCompleteCommandDict.containsKey(nextSequence)) {
            ProcessingCommand processingCommand = _messageDict.remove(nextSequence);

            if (processingCommand != null) {
                CommandResult commandResult = _requestToCompleteCommandDict.get(nextSequence);
                completeCommand(processingCommand, commandResult);
            }
            _requestToCompleteCommandDict.remove(nextSequence);
            returnSequence = nextSequence;

            nextSequence++;
        }

        return returnSequence;
    }

    private CompletableFuture completeCommand(ProcessingCommand processingCommand, CommandResult commandResult) {
        try {
            return processingCommand.completeAsync(commandResult);
        } catch (Exception ex) {
            _logger.error(String.format("Failed to complete command, commandId: %s, aggregateRootId: %s", processingCommand.getMessage().id(), processingCommand.getMessage().getAggregateRootId()), ex);
            return CompletableFuture.completedFuture(null);
        }
    }

    private void tryRun() {
        if (tryEnter()) {
            CompletableFuture.runAsync(this::run);
        }
    }

    private boolean tryEnter() {
        return _isRunning.compareAndSet(false, true);
    }

    private void exit() {
        _isRunning.getAndSet(false);
    }

    public Date getLastActiveTime() {
        return _lastActiveTime;
    }

    public boolean isRunning() {
        return _isRunning.get();
    }
}
