package org.enodeframework.commanding.impl;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.ICommandExecuteContext;
import org.enodeframework.common.exception.AggregateRootAlreadyExistException;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IAggregateStorage;
import org.enodeframework.domain.IRepository;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.queue.IMessageContext;
import org.enodeframework.queue.ISendReplyService;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.command.CommandMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    private final ISendReplyService sendReplyService;
    private final QueueMessage queueMessage;
    private final IMessageContext messageContext;
    private final CommandMessage commandMessage;
    private String result;
    private IApplicationMessage applicationMessage;

    public CommandExecuteContext(
            IRepository repository,
            IAggregateStorage aggregateRootStorage,
            QueueMessage queueMessage,
            IMessageContext messageContext,
            CommandMessage commandMessage,
            ISendReplyService sendReplyService
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
    public CompletableFuture<Boolean> onCommandExecutedAsync(CommandResult commandResult) {
        messageContext.onMessageHandled(queueMessage);
        if (Objects.isNull(commandMessage.getReplyAddress())) {
            return Task.completedTask;
        }
        return sendReplyService.sendCommandReply(commandResult, commandMessage.getReplyAddress());
    }

    @Override
    public void add(IAggregateRoot aggregateRoot) {
        Ensure.notNull(aggregateRoot, "aggregateRoot");
        if (trackingAggregateRootDict.containsKey(aggregateRoot.getUniqueId())) {
            throw new AggregateRootAlreadyExistException(aggregateRoot.getUniqueId(), aggregateRoot.getClass());
        }
        trackingAggregateRootDict.put(aggregateRoot.getUniqueId(), aggregateRoot);
    }

    /**
     * Add a new aggregate into the current command context synchronously, and then return a completed task object.
     */
    @Override
    public CompletableFuture<Boolean> addAsync(IAggregateRoot aggregateRoot) {
        add(aggregateRoot);
        return Task.completedTask;
    }

    /**
     * Get an aggregate from the current command context.
     */
    @Override
    public <T extends IAggregateRoot> CompletableFuture<T> getAsync(Object id, boolean firstFromCache, Class<T> aggregateRootType) {
        Ensure.notNull(id, "id");
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
                trackingAggregateRootDict.putIfAbsent(aggregateRoot.getUniqueId(), aggregateRoot);
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
        result = "";
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public void setResult(String result) {
        this.result = result;
    }

    /**
     * Get an application message.
     */
    @Override
    public IApplicationMessage getApplicationMessage() {
        return applicationMessage;
    }

    /**
     * Set an application message.
     */
    @Override
    public void setApplicationMessage(IApplicationMessage applicationMessage) {
        this.applicationMessage = applicationMessage;
    }
}
