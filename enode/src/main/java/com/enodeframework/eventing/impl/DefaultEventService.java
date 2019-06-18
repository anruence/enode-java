package com.enodeframework.eventing.impl;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandStatus;
import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ProcessingCommand;
import com.enodeframework.commanding.ProcessingCommandMailbox;
import com.enodeframework.common.io.IOHelper;
import com.enodeframework.common.io.Task;
import com.enodeframework.common.scheduling.IScheduleService;
import com.enodeframework.domain.IMemoryCache;
import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.eventing.EventAppendResult;
import com.enodeframework.eventing.EventCommittingContext;
import com.enodeframework.eventing.IEventService;
import com.enodeframework.eventing.IEventStore;
import com.enodeframework.infrastructure.IMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DefaultEventService implements IEventService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultEventService.class);

    private final ConcurrentMap<String, EventMailBox> mailboxDict;

    private final int batchSize;

    private final int timeoutSeconds;

    private final String taskName;

    private final int commandMailBoxPersistenceMaxBatchSize = 1000;

    private final int scanExpiredAggregateIntervalMilliseconds = 5000;

    private final int eventMailBoxPersistenceMaxBatchSize = 1000;

    private final int aggregateRootMaxInactiveSeconds = 3600 * 24 * 3;

    @Autowired
    private IScheduleService scheduleService;

    @Autowired
    private IMemoryCache memoryCache;

    @Autowired
    private IEventStore eventStore;

    @Autowired
    private IMessagePublisher<DomainEventStreamMessage> domainEventPublisher;

    public DefaultEventService() {
        this.mailboxDict = new ConcurrentHashMap<>();
        this.batchSize = eventMailBoxPersistenceMaxBatchSize;
        this.timeoutSeconds = aggregateRootMaxInactiveSeconds;
        this.taskName = "CleanInactiveAggregates" + System.nanoTime() + new Random().nextInt(10000);
    }

    @Override
    public void commitDomainEventAsync(EventCommittingContext context) {
        EventMailBox eventMailbox = mailboxDict.computeIfAbsent(context.getAggregateRoot().uniqueId(), x ->
                new EventMailBox(x, batchSize, committingContexts ->
                {
                    if (committingContexts == null || committingContexts.size() == 0) {
                        return;
                    }
                    if (eventStore.isSupportBatchAppendEvent()) {
                        batchPersistEventAsync(committingContexts, 0);
                    } else {
                        persistEventOneByOne(committingContexts);
                    }
                })
        );

        eventMailbox.enqueueMessage(context);
        refreshAggregateMemoryCache(context);
    }

    @Override
    public void publishDomainEventAsync(ProcessingCommand processingCommand, DomainEventStream eventStream) {
        if (eventStream.items() == null || eventStream.items().size() == 0) {
            eventStream.setItems(processingCommand.getItems());
        }
        DomainEventStreamMessage eventStreamMessage = new DomainEventStreamMessage(
                processingCommand.getMessage().id(), eventStream.aggregateRootId(), eventStream.version(),
                eventStream.aggregateRootTypeName(), eventStream.events(), eventStream.items());
        publishDomainEventAsync(processingCommand, eventStreamMessage, 0);
    }

    @Override
    public void start() {
        scheduleService.startTask(taskName, this::cleanInactiveMailbox, scanExpiredAggregateIntervalMilliseconds, scanExpiredAggregateIntervalMilliseconds);
    }

    @Override
    public void stop() {
        scheduleService.stopTask(taskName);
    }

    private void batchPersistEventAsync(List<EventCommittingContext> committingContexts, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("BatchPersistEventAsync",
                () -> eventStore.batchAppendAsync(committingContexts.stream().map(x -> x.getEventStream()).collect(Collectors.toList())),
                currentRetryTimes -> batchPersistEventAsync(committingContexts, currentRetryTimes),
                result ->
                {
                    EventMailBox eventMailBox = committingContexts.get(0).getEventMailBox();
                    EventAppendResult appendResult = result.getData();
                    if (appendResult == EventAppendResult.Success) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Batch persist event success, aggregateRootId: {}, eventStreamCount: {}", eventMailBox.getAggregateRootId(), committingContexts.size());
                        }

                        CompletableFuture.runAsync(() ->
                                committingContexts.forEach(context -> publishDomainEventAsync(context.getProcessingCommand(), context.getEventStream()))
                        );

                        eventMailBox.tryRun(true);
                    } else if (appendResult == EventAppendResult.DuplicateEvent) {
                        EventCommittingContext context = committingContexts.get(0);
                        if (context.getEventStream().version() == 1) {
                            handleFirstEventDuplicationAsync(context, 0);
                        } else {
                            logger.warn("Batch persist event has concurrent version conflict, first eventStream: {}, batchSize: {}", context.getEventStream(), committingContexts.size());
                            resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence());
                        }
                    } else if (appendResult == EventAppendResult.DuplicateCommand) {
                        persistEventOneByOne(committingContexts);
                    }
                },
                () -> String.format("[contextListCount:%d]", committingContexts.size()),
                errorMessage -> logger.error("Batch persist event has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage),
                retryTimes, true);
    }

    private void persistEventOneByOne(List<EventCommittingContext> contextList) {
        concatContexts(contextList);
        persistEvent(contextList.get(0), 0);
    }

    private void persistEvent(EventCommittingContext context, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("PersistEvent",
                () -> eventStore.appendAsync(context.getEventStream()),
                currentRetryTimes -> persistEvent(context, currentRetryTimes),

                result -> {
                    if (result.getData() == EventAppendResult.Success) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Persist events success, {}", context.getEventStream());
                        }
                        publishDomainEventAsync(context.getProcessingCommand(), context.getEventStream());

                        if (context.getNext() != null) {
                            persistEvent(context.getNext(), 0);
                        } else {
                            context.getEventMailBox().tryRun(true);
                        }
                    } else if (result.getData() == EventAppendResult.DuplicateEvent) {
                        //如果是当前事件的版本号为1，则认为是在创建重复的聚合根
                        if (context.getEventStream().version() == 1) {
                            handleFirstEventDuplicationAsync(context, 0);
                        }
                        //如果事件的版本大于1，则认为是更新聚合根时遇到并发冲突了，则需要进行重试；
                        else {
                            logger.warn("Persist event has concurrent version conflict, eventStream: {}", context.getEventStream());
                            resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence());
                        }
                    } else if (result.getData() == EventAppendResult.DuplicateCommand) {
                        logger.warn("Persist event has duplicate command, eventStream: {}", context.getEventStream());
                        resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1);
                        tryToRepublishEventAsync(context, 0);
                    }
                },
                () -> String.format("[eventStream:%s]", context.getEventStream()),
                errorMessage -> logger.error("Persist event has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage),
                retryTimes, true);
    }

    private CompletableFuture<Void> resetCommandMailBoxConsumingSequence(EventCommittingContext context, long consumingSequence) {
        EventMailBox eventMailBox = context.getEventMailBox();
        ProcessingCommand processingCommand = context.getProcessingCommand();
        ICommand command = processingCommand.getMessage();
        ProcessingCommandMailbox commandMailBox = processingCommand.getMailbox();

        commandMailBox.pause();
        try {
            // await 阻塞获取
            return refreshAggregateMemoryCacheToLatestVersion(context.getEventStream().aggregateRootTypeName(), context.getEventStream().aggregateRootId()).thenAccept(r -> {
                commandMailBox.resetConsumingSequence(consumingSequence);
                eventMailBox.clear();
                eventMailBox.exit();
                logger.info("ResetCommandMailBoxConsumingSequence success, commandId: {}, aggregateRootId: {}, consumingSequence: {}", command.id(), command.getAggregateRootId(), consumingSequence);
            }).exceptionally(ex -> {
                logger.error(String.format("ResetCommandMailBoxConsumingOffset has unknown exception, commandId: %s, aggregateRootId: %s", command.id(), command.getAggregateRootId()), ex);
                return null;
            });
        } catch (Exception ex) {
            logger.error(String.format("ResetCommandMailBoxConsumingOffset has unknown exception, commandId: %s, aggregateRootId: %s", command.id(), command.getAggregateRootId()), ex);
        } finally {
            commandMailBox.resume();
        }
        return Task.CompletedTask;
    }

    private void tryToRepublishEventAsync(EventCommittingContext context, int retryTimes) {
        ICommand command = context.getProcessingCommand().getMessage();

        IOHelper.tryAsyncActionRecursively("FindEventByCommandIdAsync",
                () -> eventStore.findAsync(context.getEventStream().aggregateRootId(), command.id()),
                currentRetryTimes -> tryToRepublishEventAsync(context, currentRetryTimes),
                result ->
                {
                    DomainEventStream existingEventStream = result.getData();
                    if (existingEventStream != null) {
                        //这里，我们需要再重新做一遍发布事件这个操作；
                        //之所以要这样做是因为虽然该command产生的事件已经持久化成功，但并不表示事件已经发布出去了；
                        //因为有可能事件持久化成功了，但那时正好机器断电了，则发布事件都没有做；
                        publishDomainEventAsync(context.getProcessingCommand(), existingEventStream);
                    } else {
                        //到这里，说明当前command想添加到eventStore中时，提示command重复，但是尝试从eventStore中取出该command时却找不到该command。
                        //出现这种情况，我们就无法再做后续处理了，这种错误理论上不会出现，除非eventStore的Add接口和Get接口出现读写不一致的情况；
                        //框架会记录错误日志，让开发者排查具体是什么问题。
                        String errorMessage = String.format("Command should be exist in the event store, but we cannot find it from the event store, this should not be happen, and we cannot continue again. commandType:%s, commandId:%s, aggregateRootId:%s",
                                command.getClass().getName(),
                                command.id(),
                                context.getEventStream().aggregateRootId());
                        logger.error(errorMessage);

                        CommandResult commandResult = new CommandResult(CommandStatus.Failed, command.id(), command.getAggregateRootId(), "Command should be exist in the event store, but we cannot find it from the event store.", String.class.getName());
                        completeCommand(context.getProcessingCommand(), commandResult);
                    }
                },
                () -> String.format("[aggregateRootId:%s, commandId:%s]", command.getAggregateRootId(), command.id()),
                errorMessage ->
                {
                    logger.error(String.format("Find event by commandId has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage));
                },
                retryTimes, true);
    }

    private void handleFirstEventDuplicationAsync(EventCommittingContext context, int retryTimes) {
        DomainEventStream eventStream = context.getEventStream();

        IOHelper.tryAsyncActionRecursively("FindFirstEventByVersion",
                () -> eventStore.findAsync(eventStream.aggregateRootId(), 1),
                currentRetryTimes -> handleFirstEventDuplicationAsync(context, currentRetryTimes),
                result ->
                {
                    DomainEventStream firstEventStream = result.getData();
                    if (firstEventStream != null) {
                        //判断是否是同一个command，如果是，则再重新做一遍发布事件；
                        //之所以要这样做，是因为虽然该command产生的事件已经持久化成功，但并不表示事件也已经发布出去了；
                        //有可能事件持久化成功了，但那时正好机器断电了，则发布事件都没有做；
                        if (context.getProcessingCommand().getMessage().id().equals(firstEventStream.commandId())) {
                            resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1).thenRun(() -> {
                                publishDomainEventAsync(context.getProcessingCommand(), firstEventStream);
                            });
                        } else {
                            //如果不是同一个command，则认为是两个不同的command重复创建ID相同的聚合根，我们需要记录错误日志，然后通知当前command的处理完成；
                            String errorMessage = String.format("Duplicate aggregate creation. current commandId:%s, existing commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                                    context.getProcessingCommand().getMessage().id(),
                                    firstEventStream.commandId(),
                                    firstEventStream.aggregateRootId(),
                                    firstEventStream.aggregateRootTypeName());
                            logger.error(errorMessage);
                            resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1).thenRun(() -> {
                                CommandResult commandResult = new CommandResult(CommandStatus.Failed, context.getProcessingCommand().getMessage().id(), eventStream.aggregateRootId(), "Duplicate aggregate creation.", String.class.getName());
                                completeCommand(context.getProcessingCommand(), commandResult);
                            });
                        }
                    } else {
                        String errorMessage = String.format("Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore. commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                                eventStream.commandId(),
                                eventStream.aggregateRootId(),
                                eventStream.aggregateRootTypeName());
                        logger.error(errorMessage);
                        resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1).thenRun(() -> {
                            CommandResult commandResult = new CommandResult(CommandStatus.Failed, context.getProcessingCommand().getMessage().id(), eventStream.aggregateRootId(), "Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore.", String.class.getName());
                            completeCommand(context.getProcessingCommand(), commandResult);
                        });
                    }
                },
                () -> String.format("[eventStream:%s]", eventStream),
                errorMessage -> logger.error(String.format("Find the first version of event has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
    }

    private void refreshAggregateMemoryCache(EventCommittingContext context) {
        try {
            context.getAggregateRoot().acceptChanges(context.getEventStream().version());
            memoryCache.set(context.getAggregateRoot());
        } catch (Exception ex) {
            logger.error(String.format("Refresh aggregate memory cache failed for event stream:%s", context.getEventStream()), ex);
        }
    }

    private CompletableFuture<Void> refreshAggregateMemoryCacheToLatestVersion(String aggregateRootTypeName, String aggregateRootId) {
        try {
            return memoryCache.refreshAggregateFromEventStoreAsync(aggregateRootTypeName, aggregateRootId);
        } catch (Exception ex) {
            logger.error("Refresh aggregate memory cache to latest version has unknown exception, aggregateRootTypeName:{}, aggregateRootId:{}", aggregateRootTypeName, aggregateRootId, ex);
        }
        return Task.CompletedTask;
    }

    private void publishDomainEventAsync(ProcessingCommand processingCommand, DomainEventStreamMessage eventStream, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("PublishDomainEventAsync",
                () -> domainEventPublisher.publishAsync(eventStream),
                currentRetryTimes -> publishDomainEventAsync(processingCommand, eventStream, currentRetryTimes),
                result ->
                {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Publish domain events success, {}", eventStream);
                    }

                    String commandHandleResult = processingCommand.getCommandExecuteContext().getResult();
                    CommandResult commandResult = new CommandResult(CommandStatus.Success, processingCommand.getMessage().id(), eventStream.aggregateRootId(), commandHandleResult, String.class.getName());
                    completeCommand(processingCommand, commandResult);
                },
                () -> String.format("[eventStream:%s]", eventStream),
                errorMessage -> logger.error(String.format("Publish event has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
    }

    private void concatContexts(List<EventCommittingContext> contextList) {
        for (int i = 0; i < contextList.size() - 1; i++) {
            EventCommittingContext currentContext = contextList.get(i);
            EventCommittingContext nextContext = contextList.get(i + 1);
            currentContext.setNext(nextContext);
        }
    }

    private void completeCommand(ProcessingCommand processingCommand, CommandResult commandResult) {
        processingCommand.getMailbox().completeMessage(processingCommand, commandResult);
    }

    private void cleanInactiveMailbox() {
        List<Map.Entry<String, EventMailBox>> inactiveList = mailboxDict.entrySet().stream().filter(entry ->
                entry.getValue().isInactive(timeoutSeconds) && entry.getValue().isRunning()
        ).collect(Collectors.toList());

        inactiveList.forEach(entry -> {
            if (mailboxDict.remove(entry.getKey()) != null) {
                logger.info("Removed inactive event mailbox, aggregateRootId: {}", entry.getKey());
            }
        });
    }
}
