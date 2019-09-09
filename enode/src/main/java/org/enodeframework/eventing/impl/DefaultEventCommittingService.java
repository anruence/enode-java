package org.enodeframework.eventing.impl;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ProcessingCommand;
import org.enodeframework.commanding.ProcessingCommandMailbox;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Linq;
import org.enodeframework.domain.IMemoryCache;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.EventAppendResult;
import org.enodeframework.eventing.EventCommittingContext;
import org.enodeframework.eventing.EventCommittingContextMailBox;
import org.enodeframework.eventing.IEventCommittingService;
import org.enodeframework.eventing.IEventStore;
import org.enodeframework.messaging.IMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DefaultEventCommittingService implements IEventCommittingService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultEventCommittingService.class);

    private int eventMailBoxCount;

    private List<EventCommittingContextMailBox> eventCommittingContextMailBoxList;

    @Autowired
    private IMemoryCache memoryCache;
    @Autowired
    private IEventStore eventStore;
    @Autowired
    private IMessagePublisher<DomainEventStreamMessage> domainEventPublisher;

    public DefaultEventCommittingService() {
        this(1000, 4);
    }

    public DefaultEventCommittingService(int eventMailBoxPersistenceMaxBatchSize, int eventMailBoxCount) {
        this.eventCommittingContextMailBoxList = new ArrayList<>();
        this.eventMailBoxCount = eventMailBoxCount;
        for (int i = 0; i < eventMailBoxCount; i++) {
            EventCommittingContextMailBox mailBox = new EventCommittingContextMailBox(i, eventMailBoxPersistenceMaxBatchSize, this::batchPersistEventCommittingContexts);
            eventCommittingContextMailBoxList.add(mailBox);
        }
    }

    public DefaultEventCommittingService setMemoryCache(IMemoryCache memoryCache) {
        this.memoryCache = memoryCache;
        return this;
    }

    public DefaultEventCommittingService setEventStore(IEventStore eventStore) {
        this.eventStore = eventStore;
        return this;
    }

    public DefaultEventCommittingService setDomainEventPublisher(IMessagePublisher<DomainEventStreamMessage> domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
        return this;
    }

    @Override
    public void commitDomainEventAsync(EventCommittingContext eventCommittingContext) {
        int eventMailboxIndex = getEventMailBoxIndex(eventCommittingContext.getEventStream().getAggregateRootId());
        EventCommittingContextMailBox eventMailbox = eventCommittingContextMailBoxList.get(eventMailboxIndex);
        eventMailbox.enqueueMessage(eventCommittingContext);
    }

    @Override
    public void publishDomainEventAsync(ProcessingCommand processingCommand, DomainEventStream eventStream) {
        if (eventStream.getItems() == null || eventStream.getItems().size() == 0) {
            eventStream.setItems(processingCommand.getItems());
        }
        DomainEventStreamMessage eventStreamMessage = new DomainEventStreamMessage(
                processingCommand.getMessage().getId(),
                eventStream.getAggregateRootId(),
                eventStream.getVersion(),
                eventStream.getAggregateRootTypeName(),
                eventStream.events(),
                eventStream.getItems());
        publishDomainEventAsync(processingCommand, eventStreamMessage, 0);
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
        batchPersistEventAsync(committingContexts, 0);
    }

    private void batchPersistEventAsync(List<EventCommittingContext> committingContexts, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("BatchPersistEventAsync",
                () -> eventStore.batchAppendAsync(committingContexts.stream().map(EventCommittingContext::getEventStream).collect(Collectors.toList())),
                result -> {
                    EventCommittingContextMailBox eventMailBox = Linq.first(committingContexts).getMailBox();
                    EventAppendResult appendResult = result.getData();
                    //针对持久化成功的聚合根，发布这些聚合根的事件到Q端
                    if (appendResult.getSuccessAggregateRootIdList().size() > 0) {
                        Map<String, List<EventCommittingContext>> successCommittedContextDict = committingContexts.stream()
                                .filter(x -> appendResult.getSuccessAggregateRootIdList().contains(x.getEventStream().getAggregateRootId()))
                                .collect(Collectors.groupingBy(x -> x.getEventStream().getAggregateRootId()));
                        if (logger.isDebugEnabled()) {
                            logger.debug("Batch persist events, mailboxNumber: {}, succeedAggregateRootCount: {}, detail: {}",
                                    eventMailBox.getNumber(),
                                    appendResult.getSuccessAggregateRootIdList().size(),
                                    JsonTool.serialize(appendResult.getSuccessAggregateRootIdList()));
                        }

                        CompletableFuture.runAsync(() -> {
                            successCommittedContextDict.values().forEach(x -> {
                                for (EventCommittingContext context : x) {
                                    publishDomainEventAsync(context.getProcessingCommand(), context.getEventStream());
                                }
                            });
                        });
                    }
                    //针对持久化出现重复的命令ID，则重新发布这些命令对应的领域事件到Q端
                    if (appendResult.getDuplicateCommandIdList().size() > 0) {
                        logger.warn("Batch persist events, mailboxNumber: {}, duplicateCommandIdCount: {}, detail: {}",
                                eventMailBox.getNumber(),
                                appendResult.getDuplicateCommandIdList().size(),
                                JsonTool.serialize(appendResult.getDuplicateCommandIdList()));

                        for (String commandId : appendResult.getDuplicateCommandIdList()) {
                            Optional<EventCommittingContext> committingContextOptional = committingContexts.stream().filter(x -> x.getProcessingCommand().getMessage().getId().equals(commandId)).findFirst();
                            if (committingContextOptional.isPresent()) {
                                EventCommittingContext committingContext = committingContextOptional.get();
                                ProcessingCommandMailbox commandMailBox = committingContext.getProcessingCommand().getMailBox();
                                resetCommandMailBoxConsumingSequence(committingContext, committingContext.getProcessingCommand().getSequence() + 1);
                                tryToRepublishEventAsync(committingContext, 0);
                            }

                        }
                    }

                    //针对持久化出现版本冲突的聚合根，则自动处理每个聚合根的冲突
                    if (appendResult.getDuplicateEventAggregateRootIdList().size() > 0) {
                        logger.warn("Batch persist events, mailboxNumber: {}, duplicateEventAggregateRootCount: {}, detail: {}",
                                eventMailBox.getNumber(),
                                appendResult.getDuplicateEventAggregateRootIdList().size(),
                                JsonTool.serialize(appendResult.getDuplicateEventAggregateRootIdList()));

                        for (String aggregateRootId : appendResult.getDuplicateEventAggregateRootIdList()) {
                            Optional<EventCommittingContext> committingContextOptional = committingContexts.stream().filter(x -> x.getEventStream().getAggregateRootId().equals(aggregateRootId)).findFirst();
                            if (committingContextOptional.isPresent()) {
                                EventCommittingContext context = committingContextOptional.get();
                                processAggregateDuplicateEvent(context);
                            }
                        }
                    }
                    //最终，将当前的EventMailBox的本次处理标记为处理完成，然后继续可以处理下一批事件
                    eventMailBox.completeRun();
                },
                () -> String.format("[contextListCount:%d]", committingContexts.size()),
                errorMessage -> logger.error("Batch persist event has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage),
                retryTimes, true);
    }

    private void processAggregateDuplicateEvent(EventCommittingContext eventCommittingContext) {
        if (eventCommittingContext.getEventStream().getVersion() == 1) {
            handleFirstEventDuplicationAsync(eventCommittingContext, 0);
        } else {
            resetCommandMailBoxConsumingSequence(eventCommittingContext, eventCommittingContext.getProcessingCommand().getSequence());
        }
    }

    private CompletableFuture<Void> resetCommandMailBoxConsumingSequence(EventCommittingContext context, long consumingSequence) {
        ProcessingCommandMailbox commandMailBox = context.getProcessingCommand().getMailBox();
        EventCommittingContextMailBox eventMailBox = context.getMailBox();
        String aggregateRootId = context.getEventStream().getAggregateRootId();
        commandMailBox.pause();
        CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            eventMailBox.removeAggregateAllEventCommittingContexts(aggregateRootId);
            future = memoryCache.refreshAggregateFromEventStoreAsync(context.getEventStream().getAggregateRootTypeName(), aggregateRootId).thenAccept(x -> {
                try {
                    commandMailBox.resetConsumingSequence(consumingSequence);
                } finally {
                    commandMailBox.resume();
                    commandMailBox.tryRun();
                }
            });
        } catch (Exception ex) {
            future.completeExceptionally(ex);
        }
        return future.exceptionally(ex -> {
            logger.error("ResetCommandMailBoxConsumingSequence has unknown exception, aggregateRootId: {}", aggregateRootId, ex);
            return null;
        });
    }

    private void tryToRepublishEventAsync(EventCommittingContext context, int retryTimes) {
        ICommand command = context.getProcessingCommand().getMessage();
        IOHelper.tryAsyncActionRecursively("FindEventByCommandIdAsync",
                () -> eventStore.findAsync(context.getEventStream().getAggregateRootId(), command.getId()),
                result -> {
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
                errorMessage -> {
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
                            resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1).thenAccept(x -> {
                                publishDomainEventAsync(context.getProcessingCommand(), firstEventStream);
                            });
                        } else {
                            //如果不是同一个command，则认为是两个不同的command重复创建ID相同的聚合根，我们需要记录错误日志，然后通知当前command的处理完成；
                            String errorMessage = String.format("Duplicate aggregate creation. current commandId:%s, existing commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                                    context.getProcessingCommand().getMessage().getId(),
                                    firstEventStream.getCommandId(),
                                    firstEventStream.getAggregateRootId(),
                                    firstEventStream.getAggregateRootTypeName());
                            logger.error(errorMessage);
                            resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1).thenAccept(x -> {
                                CommandResult commandResult = new CommandResult(CommandStatus.Failed, context.getProcessingCommand().getMessage().getId(), context.getEventStream().getAggregateRootId(), "Duplicate aggregate creation.", String.class.getName());
                                completeCommand(context.getProcessingCommand(), commandResult);
                            });
                        }
                    } else {
                        String errorMessage = String.format("Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore. commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                                context.getEventStream().getCommandId(),
                                context.getEventStream().getAggregateRootId(),
                                context.getEventStream().getAggregateRootTypeName());
                        logger.error(errorMessage);
                        resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1).thenAccept(x -> {
                            CommandResult commandResult = new CommandResult(CommandStatus.Failed, context.getProcessingCommand().getMessage().getId(), context.getEventStream().getAggregateRootId(), "Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore.", String.class.getName());
                            completeCommand(context.getProcessingCommand(), commandResult);
                        });
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

    private CompletableFuture<Void> completeCommand(ProcessingCommand processingCommand, CommandResult commandResult) {
        return processingCommand.getMailBox().completeMessage(processingCommand, commandResult);
    }
}