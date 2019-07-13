package com.enodeframework.commanding.impl;

import com.enodeframework.commanding.AggregateRootAlreadyExistException;
import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandReturnType;
import com.enodeframework.commanding.ICommandExecuteContext;
import com.enodeframework.common.io.Task;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.domain.IAggregateStorage;
import com.enodeframework.domain.IRepository;
import com.enodeframework.queue.IMessageContext;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.SendReplyService;
import com.enodeframework.queue.command.CommandMessage;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author anruence@gmail.com
 */
public class CommandExecuteContext implements ICommandExecuteContext {
    private final ConcurrentMap<String, IAggregateRoot> trackingAggregateRootDict;
    private final IRepository repository;
    private final IAggregateStorage aggregateRootStorage;
    private final SendReplyService sendReplyService;
    private final QueueMessage queueMessage;
    private String result;
    private IMessageContext messageContext;
    private CommandMessage commandMessage;

    public CommandExecuteContext(
            IRepository repository,
            IAggregateStorage aggregateRootStorage,
            QueueMessage queueMessage,
            IMessageContext messageContext,
            CommandMessage commandMessage,
            SendReplyService sendReplyService
    ) {
        this.trackingAggregateRootDict = new ConcurrentHashMap<>();
        this.repository = repository;
        this.aggregateRootStorage = aggregateRootStorage;
        this.sendReplyService = sendReplyService;
        this.queueMessage = queueMessage;
        this.commandMessage = commandMessage;
        this.messageContext = messageContext;
    }

    @Override
    public CompletableFuture<Void> onCommandExecutedAsync(CommandResult commandResult) {
        messageContext.onMessageHandled(queueMessage);
        if (Strings.isNullOrEmpty(commandMessage.getReplyAddress())) {
            return Task.completedTask;
        }
        return sendReplyService.sendReply(CommandReturnType.CommandExecuted.getValue(), commandResult, commandMessage.getReplyAddress());
    }

    @Override
    public void add(IAggregateRoot aggregateRoot) {
        if (aggregateRoot == null) {
            throw new NullPointerException("aggregateRoot");
        }

        if (trackingAggregateRootDict.containsKey(aggregateRoot.uniqueId())) {
            throw new AggregateRootAlreadyExistException(aggregateRoot.uniqueId(), aggregateRoot.getClass());
        }

        trackingAggregateRootDict.put(aggregateRoot.uniqueId(), aggregateRoot);
    }

    /**
     * Add a new aggregate into the current command context synchronously, and then return a completed task object.
     *
     * @param aggregateRoot
     * @return
     */
    @Override
    public CompletableFuture<Void> addAsync(IAggregateRoot aggregateRoot) {
        add(aggregateRoot);
        return Task.completedTask;
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
        T iAggregateRoot = (T) trackingAggregateRootDict.get(aggregateRootId);
        CompletableFuture<T> future = new CompletableFuture<>();
        if (iAggregateRoot != null) {
            future.complete(iAggregateRoot);
            return future;
        }
        if (firstFromCache) {
            future = repository.getAsync(aggregateRootType, id);
        } else {
            future = aggregateRootStorage.getAsync(aggregateRootType, aggregateRootId);
        }
        return future.thenApply(aggregateRoot -> {
            if (aggregateRoot != null) {
                trackingAggregateRootDict.putIfAbsent(aggregateRoot.uniqueId(), aggregateRoot);
            }
            return aggregateRoot;
        });
    }

    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object id, Class<T> clazz) {
        return getAsync(id, true, clazz);
    }

    @Override
    public List<IAggregateRoot> getTrackedAggregateRoots() {
        return new ArrayList<>(trackingAggregateRootDict.values());
    }

    @Override
    public void clear() {
        trackingAggregateRootDict.clear();
        result = null;
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public void setResult(String result) {
        this.result = result;
    }
}
