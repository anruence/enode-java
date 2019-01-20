package com.enode.commanding.impl;

import com.enode.commanding.AggregateRootAlreadyExistException;
import com.enode.commanding.CommandResult;
import com.enode.commanding.CommandReturnType;
import com.enode.commanding.ICommandExecuteContext;
import com.enode.domain.IAggregateRoot;
import com.enode.domain.IAggregateStorage;
import com.enode.domain.IRepository;
import com.enode.rocketmq.SendReplyService;
import com.enode.rocketmq.client.consumer.listener.CompletableConsumeConcurrentlyContext;
import com.enode.rocketmq.command.CommandMessage;
import com.enode.rocketmq.command.ConsumeStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CommandExecuteContext<T extends Object> implements ICommandExecuteContext {
    private final ConcurrentMap<String, IAggregateRoot> _trackingAggregateRootDict;
    private final IRepository _repository;
    private final IAggregateStorage _aggregateRootStorage;
    private final SendReplyService _sendReplyService;
    private final T _queueMessage;
    private String _result;
    private CompletableConsumeConcurrentlyContext _messageContext;
    private CompletableFuture _consumeResultFuture;
    private CommandMessage _commandMessage;

    public CommandExecuteContext(
            IRepository repository, IAggregateStorage aggregateRootStorage, T queueMessage,
            CompletableConsumeConcurrentlyContext messageContext, CommandMessage commandMessage,
            SendReplyService sendReplyService, CompletableFuture consumeResultFuture) {
        _trackingAggregateRootDict = new ConcurrentHashMap<>();
        _repository = repository;
        _aggregateRootStorage = aggregateRootStorage;
        _sendReplyService = sendReplyService;
        _queueMessage = queueMessage;
        _commandMessage = commandMessage;
        _messageContext = messageContext;
        _consumeResultFuture = consumeResultFuture;
    }

    @Override
    public CompletableFuture onCommandExecutedAsync(CommandResult commandResult) {
        _consumeResultFuture.complete(ConsumeStatus.CONSUME_SUCCESS);

        if (_commandMessage.getReplyAddress() == null) {
            return CompletableFuture.completedFuture(null);
        }
        return _sendReplyService.sendReply(CommandReturnType.CommandExecuted.getValue(), commandResult, _commandMessage.getReplyAddress());
    }

    @Override
    public void add(IAggregateRoot aggregateRoot) {
        if (aggregateRoot == null) {
            throw new NullPointerException("aggregateRoot");
        }

        if (_trackingAggregateRootDict.containsKey(aggregateRoot.uniqueId())) {
            throw new AggregateRootAlreadyExistException(aggregateRoot.uniqueId(), aggregateRoot.getClass());
        }

        _trackingAggregateRootDict.put(aggregateRoot.uniqueId(), aggregateRoot);
    }

    /**
     * Add a new aggregate into the current command context synchronously, and then return a completed task object.
     *
     * @param aggregateRoot
     * @return
     */
    @Override
    public CompletableFuture addAsync(IAggregateRoot aggregateRoot) {
        return CompletableFuture.supplyAsync(() -> {
            add(aggregateRoot);
            return true;
        });
    }

    /**
     * Get an aggregate from the current command context.
     *
     * @param id
     * @param firstFromCache
     * @return
     */
    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object id, boolean firstFromCache, Class<T> aggregateRootType) {
        if (id == null) {
            throw new NullPointerException("id");
        }
        String aggregateRootId = id.toString();
        T iAggregateRoot = (T) _trackingAggregateRootDict.get(aggregateRootId);
        CompletableFuture<T> future = new CompletableFuture<>();
        if (iAggregateRoot != null) {
            future.complete(iAggregateRoot);
            return future;
        }
        if (firstFromCache) {
            future = _repository.getAsync(aggregateRootType, id);
        } else {
            future = _aggregateRootStorage.getAsync(aggregateRootType, aggregateRootId);
        }
        return future.thenApply(aggregateRoot -> {
            if (aggregateRoot != null) {
                _trackingAggregateRootDict.putIfAbsent(aggregateRoot.uniqueId(), aggregateRoot);
            }
            return aggregateRoot;
        });
    }

    @Override
    public List<IAggregateRoot> getTrackedAggregateRoots() {
        return new ArrayList<>(_trackingAggregateRootDict.values());
    }

    @Override
    public void clear() {
        _trackingAggregateRootDict.clear();
        _result = null;
    }

    @Override
    public String getResult() {
        return _result;
    }

    @Override
    public void setResult(String result) {
        _result = result;
    }
}
