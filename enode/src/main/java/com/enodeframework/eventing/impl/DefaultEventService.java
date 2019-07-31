package com.enodeframework.eventing.impl;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandStatus;
import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ProcessingCommand;
import com.enodeframework.common.io.IOHelper;
import com.enodeframework.common.scheduling.IScheduleService;
import com.enodeframework.common.utilities.Linq;
import com.enodeframework.domain.IMemoryCache;
import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.DomainEventStreamMessage;
import com.enodeframework.eventing.EventAppendResult;
import com.enodeframework.eventing.EventCommittingContext;
import com.enodeframework.eventing.IEventService;
import com.enodeframework.eventing.IEventStore;
import com.enodeframework.infrastructure.IMailBox;
import com.enodeframework.infrastructure.IMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.enodeframework.common.io.Task.await;

/**
 * @author anruence@gmail.com
 */
public class DefaultEventService implements IEventService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultEventService.class);
    private int timeoutSeconds;
    private String taskName;
    private int eventMailBoxCount;
    private int scanExpiredAggregateIntervalMilliseconds = 5000;
    private List<EventMailBox> eventMailBoxList;
    @Autowired
    private IScheduleService scheduleService;
    @Autowired
    private IMemoryCache memoryCache;
    @Autowired
    private IEventStore eventStore;
    @Autowired
    private IMessagePublisher<DomainEventStreamMessage> domainEventPublisher;

    public DefaultEventService() {
        this(1000, 5000, 4);
    }

    public DefaultEventService(int eventMailBoxPersistenceMaxBatchSize, int timeoutSeconds, int eventMailBoxCount) {
        this.eventMailBoxList = new ArrayList<>();
        this.timeoutSeconds = timeoutSeconds;
        this.eventMailBoxCount = eventMailBoxCount;
        this.taskName = "CleanInactiveAggregates" + System.nanoTime() + new Random().nextInt(10000);
        for (int i = 0; i < eventMailBoxCount; i++) {
            EventMailBox mailBox = new EventMailBox(String.valueOf(i), eventMailBoxPersistenceMaxBatchSize, this::batchPersistEventCommittingContexts);
            eventMailBoxList.add(mailBox);
        }
    }

    public DefaultEventService setScheduleService(IScheduleService scheduleService) {
        this.scheduleService = scheduleService;
        return this;
    }

    public DefaultEventService setMemoryCache(IMemoryCache memoryCache) {
        this.memoryCache = memoryCache;
        return this;
    }

    public DefaultEventService setEventStore(IEventStore eventStore) {
        this.eventStore = eventStore;
        return this;
    }

    public DefaultEventService setDomainEventPublisher(IMessagePublisher<DomainEventStreamMessage> domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
        return this;
    }

    private int getEventMailBoxIndex(String aggregateRootId) {
        int hash = 23;
        for (char c : aggregateRootId.toCharArray()) {
            hash = (hash << 5) - hash + c;
        }
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash % eventMailBoxCount;
    }

    private void batchPersistEventCommittingContexts(List<EventCommittingContext> committingContexts) {
        if (committingContexts == null || committingContexts.size() == 0) {
            return;
        }
        if (eventStore.isSupportBatchAppendEvent()) {
            batchPersistEventAsync(committingContexts, 0);
        } else {
            persistEventOneByOne(committingContexts);
        }
    }

    @Override
    public void commitDomainEventAsync(EventCommittingContext eventCommittingContext) {
        int eventMailboxIndex = getEventMailBoxIndex(eventCommittingContext.getEventStream().getAggregateRootId());
        EventMailBox eventMailbox = eventMailBoxList.get(eventMailboxIndex);
        eventMailbox.enqueueMessage(eventCommittingContext);
    }

    @Override
    public void publishDomainEventAsync(ProcessingCommand processingCommand, DomainEventStream eventStream) {
        if (eventStream.getItems() == null || eventStream.getItems().size() == 0) {
            eventStream.setItems(processingCommand.getItems());
        }
        DomainEventStreamMessage eventStreamMessage = new DomainEventStreamMessage(
                processingCommand.getMessage().getId(), eventStream.getAggregateRootId(), eventStream.getVersion(),
                eventStream.getAggregateRootTypeName(), eventStream.events(), eventStream.getItems());
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
                () -> eventStore.batchAppendAsync(committingContexts.stream().map(EventCommittingContext::getEventStream).collect(Collectors.toList())),
                result ->
                {
                    EventAppendResult appendResult = result.getData();
                    if (appendResult == EventAppendResult.Success) {
                        IMailBox eventMailBox = Linq.first(committingContexts).getMailBox();
                        if (logger.isDebugEnabled()) {
                            logger.debug("Batch persist event success, routingKey: {}, eventStreamCount: {}, minEventVersion: {}, maxEventVersion: {}", eventMailBox.getRoutingKey(), committingContexts.size(), Linq.first(committingContexts).getEventStream().getVersion(), Linq.last(committingContexts).getEventStream().getVersion());
                        }
                        committingContexts.forEach(context -> publishDomainEventAsync(context.getProcessingCommand(), context.getEventStream()));
                        for (EventCommittingContext committingContext : committingContexts) {
                            committingContext.getMailBox().completeMessage(committingContext, true);
                        }
                        eventMailBox.completeRun();
                    } else if (appendResult == EventAppendResult.DuplicateEvent) {
                        IMailBox eventMailBox = Linq.first(committingContexts).getMailBox();
                        logger.warn("Batch persist event has concurrent version conflict, routingKey: {}, eventStreamCount: {}, minEventVersion: {}, maxEventVersion: {}", eventMailBox.getRoutingKey(), committingContexts.size(), Linq.first(committingContexts).getEventStream().getVersion(), Linq.last(committingContexts).getEventStream().getVersion());
                        processDuplicateEvent(Linq.first(committingContexts));
                    } else if (appendResult == EventAppendResult.DuplicateCommand) {
                        persistEventOneByOne(committingContexts);
                    }
                },
                () -> String.format("[contextListCount:%d]", committingContexts.size()),
                errorMessage -> logger.error("Batch persist event has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage),
                retryTimes, true);
    }

    private void processDuplicateEvent(EventCommittingContext eventCommittingContext) {
        if (eventCommittingContext.getEventStream().getVersion() == 1) {
            handleFirstEventDuplicationAsync(eventCommittingContext, 0);
        } else {
            resetCommandMailBoxConsumingSequence(eventCommittingContext, eventCommittingContext.getProcessingCommand().getSequence());
        }
    }

    private void persistEventOneByOne(List<EventCommittingContext> contextList) {
        concatContexts(contextList);
        persistEvent(Linq.first(contextList), 0);
    }

    private void persistEvent(EventCommittingContext context, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("PersistEvent",
                () -> eventStore.appendAsync(context.getEventStream()),
                result -> {
                    if (result.getData() == EventAppendResult.Success) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Persist events success, {}", context.getEventStream());
                        }
                        publishDomainEventAsync(context.getProcessingCommand(), context.getEventStream());
                        if (context.getNext() != null) {
                            persistEvent(context.getNext(), 0);
                        } else {
                            context.getMailBox().completeMessage(context, true);
                            context.getMailBox().completeRun();
                        }
                    } else if (result.getData() == EventAppendResult.DuplicateEvent) {
                        logger.warn("Persist event has concurrent version conflict, eventStream: {}", context.getEventStream());
                        processDuplicateEvent(context);
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

    private void resetCommandMailBoxConsumingSequence(EventCommittingContext context, long consumingSequence) {
        ProcessingCommand processingCommand = context.getProcessingCommand();
        ICommand command = processingCommand.getMessage();
        IMailBox commandMailBox = processingCommand.getMailBox();
        EventMailBox eventMailBox = (EventMailBox) context.getMailBox();
        String aggregateRootId = context.getEventStream().getAggregateRootId();
        commandMailBox.pause();
        try {
            eventMailBox.removeAggregateAllEventCommittingContexts(aggregateRootId);
            // await 阻塞获取
            await(memoryCache.refreshAggregateFromEventStoreAsync(context.getEventStream().getAggregateRootTypeName(), aggregateRootId));
            commandMailBox.resetConsumingSequence(consumingSequence);
        } catch (Exception ex) {
            logger.error(String.format("ResetCommandMailBoxConsumingOffset has unknown exception, commandId: %s, aggregateRootId: %s", command.getId(), command.getAggregateRootId()), ex);
        } finally {
            commandMailBox.resume();
            commandMailBox.tryRun();
            eventMailBox.completeRun();
        }
    }

    private void tryToRepublishEventAsync(EventCommittingContext context, int retryTimes) {
        ICommand command = context.getProcessingCommand().getMessage();
        IOHelper.tryAsyncActionRecursively("FindEventByCommandIdAsync",
                () -> eventStore.findAsync(context.getEventStream().getAggregateRootId(), command.getId()),
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
                                command.getId(),
                                context.getEventStream().getAggregateRootId());
                        logger.error(errorMessage);
                        CommandResult commandResult = new CommandResult(CommandStatus.Failed, command.getId(), command.getAggregateRootId(), "Command should be exist in the event store, but we cannot find it from the event store.", String.class.getName());
                        completeCommand(context.getProcessingCommand(), commandResult);
                    }
                },
                () -> String.format("[aggregateRootId:%s, commandId:%s]", command.getAggregateRootId(), command.getId()),
                errorMessage ->
                {
                    logger.error(String.format("Find event by commandId has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage));
                },
                retryTimes, true);
    }

    private void handleFirstEventDuplicationAsync(EventCommittingContext context, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("FindFirstEventByVersion",
                () -> eventStore.findAsync(context.getEventStream().getAggregateRootId(), 1),
                result ->
                {
                    DomainEventStream firstEventStream = result.getData();
                    if (firstEventStream != null) {
                        //判断是否是同一个command，如果是，则再重新做一遍发布事件；
                        //之所以要这样做，是因为虽然该command产生的事件已经持久化成功，但并不表示事件也已经发布出去了；
                        //有可能事件持久化成功了，但那时正好机器断电了，则发布事件都没有做；
                        if (context.getProcessingCommand().getMessage().getId().equals(firstEventStream.getCommandId())) {
                            resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1);
                            publishDomainEventAsync(context.getProcessingCommand(), firstEventStream);
                        } else {
                            //如果不是同一个command，则认为是两个不同的command重复创建ID相同的聚合根，我们需要记录错误日志，然后通知当前command的处理完成；
                            String errorMessage = String.format("Duplicate aggregate creation. current commandId:%s, existing commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                                    context.getProcessingCommand().getMessage().getId(),
                                    firstEventStream.getCommandId(),
                                    firstEventStream.getAggregateRootId(),
                                    firstEventStream.getAggregateRootTypeName());
                            logger.error(errorMessage);
                            resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1);
                            CommandResult commandResult = new CommandResult(CommandStatus.Failed, context.getProcessingCommand().getMessage().getId(), context.getEventStream().getAggregateRootId(), "Duplicate aggregate creation.", String.class.getName());
                            completeCommand(context.getProcessingCommand(), commandResult);
                        }
                    } else {
                        String errorMessage = String.format("Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore. commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                                context.getEventStream().getCommandId(),
                                context.getEventStream().getAggregateRootId(),
                                context.getEventStream().getAggregateRootTypeName());
                        logger.error(errorMessage);
                        resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1);
                        CommandResult commandResult = new CommandResult(CommandStatus.Failed, context.getProcessingCommand().getMessage().getId(), context.getEventStream().getAggregateRootId(), "Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore.", String.class.getName());
                        completeCommand(context.getProcessingCommand(), commandResult);
                    }
                },
                () -> String.format("[eventStream:%s]", context.getEventStream()),
                errorMessage -> logger.error("Find the first version of event has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage),
                retryTimes, true);
    }

    private void publishDomainEventAsync(ProcessingCommand processingCommand, DomainEventStreamMessage eventStream, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("PublishDomainEventAsync",
                () -> domainEventPublisher.publishAsync(eventStream),
                result ->
                {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Publish domain events success, {}", eventStream);
                    }
                    String commandHandleResult = processingCommand.getCommandExecuteContext().getResult();
                    CommandResult commandResult = new CommandResult(CommandStatus.Success, processingCommand.getMessage().getId(), eventStream.getAggregateRootId(), commandHandleResult, String.class.getName());
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
        processingCommand.getMailBox().completeMessage(processingCommand, commandResult);
    }

    private void cleanInactiveMailbox() {
//        List<EventMailBox> inactiveList = eventMailBoxList.stream().filter(entry ->
//                entry.isInactive(timeoutSeconds) && entry.isRunning()
//        ).collect(Collectors.toList());
//
//        inactiveList.forEach(entry -> {
//            if (mailboxDict.remove(entry.getKey()) != null) {
//                logger.info("Removed inactive event mailbox, aggregateRootId: {}", entry.getKey());
//            }
//        });
    }
}
