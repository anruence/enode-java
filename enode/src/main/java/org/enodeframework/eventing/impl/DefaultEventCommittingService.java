package org.enodeframework.eventing.impl;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ProcessingCommand;
import org.enodeframework.commanding.ProcessingCommandMailbox;
import org.enodeframework.common.exception.MailBoxInvalidException;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.serializing.ISerializeService;
import org.enodeframework.domain.IMemoryCache;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.DomainEventStreamMessage;
import org.enodeframework.eventing.EventCommittingContext;
import org.enodeframework.eventing.EventCommittingContextMailBox;
import org.enodeframework.eventing.IEventCommittingService;
import org.enodeframework.eventing.IEventStore;
import org.enodeframework.messaging.IMessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DefaultEventCommittingService implements IEventCommittingService {
    private static final Logger logger = LoggerFactory.getLogger(DefaultEventCommittingService.class);

    private final int eventMailBoxCount;
    private final IMemoryCache memoryCache;
    private final IEventStore eventStore;
    private final Executor executor;
    private final ISerializeService serializeService;
    private final IMessagePublisher<DomainEventStreamMessage> domainEventPublisher;
    private final List<EventCommittingContextMailBox> eventCommittingContextMailBoxList;

    public DefaultEventCommittingService(IMemoryCache memoryCache, IEventStore eventStore, ISerializeService serializeService, IMessagePublisher<DomainEventStreamMessage> domainEventPublisher, Executor executor) {
        this(memoryCache, eventStore, serializeService, domainEventPublisher, 4, executor);
    }

    public DefaultEventCommittingService(IMemoryCache memoryCache, IEventStore eventStore, ISerializeService serializeService, IMessagePublisher<DomainEventStreamMessage> domainEventPublisher, int eventMailBoxCount, Executor executor) {
        this.memoryCache = memoryCache;
        this.eventStore = eventStore;
        this.serializeService = serializeService;
        this.domainEventPublisher = domainEventPublisher;
        this.eventMailBoxCount = eventMailBoxCount;
        this.executor = executor;
        this.eventCommittingContextMailBoxList = new ArrayList<>();
        for (int i = 0; i < this.eventMailBoxCount; i++) {
            EventCommittingContextMailBox mailBox = new EventCommittingContextMailBox(i, 1000, x -> batchPersistEventAsync(x, 0), this.executor);
            eventCommittingContextMailBoxList.add(mailBox);
        }
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

    private void batchPersistEventAsync(List<EventCommittingContext> committingContexts, int retryTimes) {
        if (committingContexts == null || committingContexts.size() == 0) {
            return;
        }
        IOHelper.tryAsyncActionRecursively("BatchPersistEventAsync",
                () -> eventStore.batchAppendAsync(committingContexts.stream().map(EventCommittingContext::getEventStream).collect(Collectors.toList())),
                result -> {
                    EventCommittingContextMailBox eventMailBox = committingContexts.stream()
                            .findFirst()
                            .orElseThrow(() -> new MailBoxInvalidException("eventMailBox can not be null"))
                            .getMailBox();
                    if (result == null) {
                        logger.error("Batch persist events success, but the persist result is null, the current event committing mailbox should be pending, mailboxNumber: {}", eventMailBox.getNumber());
                        return;
                    }
                    //针对持久化成功的聚合根，正常发布这些聚合根的事件到Q端
                    if (result.getSuccessAggregateRootIdList().size() > 0) {
                        for (String aggregateRootId : result.getSuccessAggregateRootIdList()) {
                            List<EventCommittingContext> committingContextList = committingContexts.stream()
                                    .filter(x -> x.getEventStream().getAggregateRootId().equals(aggregateRootId))
                                    .collect(Collectors.toList());
                            if (committingContextList.size() > 0) {
                                for (EventCommittingContext committingContext : committingContextList) {
                                    publishDomainEventAsync(committingContext.getProcessingCommand(), committingContext.getEventStream());
                                }
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Batch persist events success, mailboxNumber: {}, aggregateRootId: {}",
                                            eventMailBox.getNumber(),
                                            aggregateRootId);
                                }
                            }
                        }
                    }
                    //针对持久化出现重复的命令ID，在命令MailBox中标记为已重复，在事件MailBox中清除对应聚合根产生的事件，且重新发布这些命令对应的领域事件到Q端
                    if (result.getDuplicateCommandAggregateRootIdList().size() > 0) {
                        for (Map.Entry<String, List<String>> entry : result.getDuplicateCommandAggregateRootIdList().entrySet()) {
                            Optional<EventCommittingContext> committingContextOptional = committingContexts.stream()
                                    .filter(x -> entry.getKey().equals(x.getEventStream().getAggregateRootId()))
                                    .findFirst();
                            if (committingContextOptional.isPresent()) {
                                logger.warn("Batch persist events has duplicate commandIds, mailboxNumber: {}, aggregateRootId: {}, commandIds: {}",
                                        eventMailBox.getNumber(),
                                        entry.getKey(),
                                        String.join(",", entry.getValue()));
                                EventCommittingContext committingContext = committingContextOptional.get();
                                resetCommandMailBoxConsumingSequence(committingContext, committingContext.getProcessingCommand().getSequence() + 1, entry.getValue())
                                        .thenAccept(x -> tryToRepublishEventAsync(committingContext, 0));
                            }
                        }
                    }

                    //针对持久化出现版本冲突的聚合根，则自动处理每个聚合根的冲突
                    if (result.getDuplicateEventAggregateRootIdList().size() > 0) {
                        for (String aggregateRootId : result.getDuplicateEventAggregateRootIdList()) {
                            Optional<EventCommittingContext> committingContextOptional = committingContexts.stream().filter(x -> x.getEventStream().getAggregateRootId().equals(aggregateRootId)).findFirst();
                            if (committingContextOptional.isPresent()) {
                                logger.warn("Batch persist events, mailboxNumber: {}, duplicateEventAggregateRootCount: {}, detail: {}",
                                        eventMailBox.getNumber(),
                                        result.getDuplicateEventAggregateRootIdList().size(),
                                        serializeService.serialize(result.getDuplicateEventAggregateRootIdList()));
                                EventCommittingContext eventCommittingContext = committingContextOptional.get();
                                if (eventCommittingContext.getEventStream().getVersion() == 1) {
                                    handleFirstEventDuplicationAsync(eventCommittingContext, 0);
                                } else {
                                    resetCommandMailBoxConsumingSequence(eventCommittingContext, eventCommittingContext.getProcessingCommand().getSequence(), null);
                                }
                            }
                        }
                    }
                    //最终，将当前的EventMailBox的本次处理标记为处理完成，然后继续可以处理下一批事件
                    eventMailBox.completeRun();
                },
                () -> String.format("[contextListCount:%d]", committingContexts.size()),
                null, retryTimes, true);
    }

    private CompletableFuture<Void> resetCommandMailBoxConsumingSequence(EventCommittingContext context, long consumingSequence, List<String> duplicateCommandIdList) {
        ProcessingCommandMailbox commandMailBox = context.getProcessingCommand().getMailBox();
        EventCommittingContextMailBox eventMailBox = context.getMailBox();
        String aggregateRootId = context.getEventStream().getAggregateRootId();
        commandMailBox.pause();
        eventMailBox.removeAggregateAllEventCommittingContexts(aggregateRootId);
        return memoryCache.refreshAggregateFromEventStoreAsync(context.getEventStream().getAggregateRootTypeName(), aggregateRootId).thenAccept(x -> {
            try {
                if (duplicateCommandIdList != null) {
                    for (String commandId : duplicateCommandIdList) {
                        commandMailBox.addDuplicateCommandId(commandId);
                    }
                }
                commandMailBox.resetConsumingSequence(consumingSequence);
            } finally {
                commandMailBox.resume();
                commandMailBox.tryRun();
            }
        }).exceptionally(ex -> {
            logger.error("ResetCommandMailBoxConsumingSequence has unknown exception, aggregateRootId: {}", aggregateRootId, ex);
            return null;
        });
    }

    private void tryToRepublishEventAsync(EventCommittingContext context, int retryTimes) {
        ICommand command = context.getProcessingCommand().getMessage();
        IOHelper.tryAsyncActionRecursively("FindEventByCommandIdAsync",
                () -> eventStore.findAsync(context.getEventStream().getAggregateRootId(), command.getId()),
                result -> {
                    if (result != null) {
                        //这里，我们需要再重新做一遍发布事件这个操作；
                        //之所以要这样做是因为虽然该command产生的事件已经持久化成功，但并不表示事件已经发布出去了；
                        //因为有可能事件持久化成功了，但那时正好机器断电了，则发布事件都没有做；
                        publishDomainEventAsync(context.getProcessingCommand(), result);
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
                null,
                retryTimes, true);
    }

    private CompletableFuture<Void> handleFirstEventDuplicationAsync(EventCommittingContext context, int retryTimes) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        IOHelper.tryAsyncActionRecursively("FindFirstEventByVersion",
                () -> eventStore.findAsync(context.getEventStream().getAggregateRootId(), 1),
                result -> {
                    if (result != null) {
                        //判断是否是同一个command，如果是，则再重新做一遍发布事件；
                        //之所以要这样做，是因为虽然该command产生的事件已经持久化成功，但并不表示事件也已经发布出去了；
                        //有可能事件持久化成功了，但那时正好机器断电了，则发布事件都没有做；
                        if (context.getProcessingCommand().getMessage().getId().equals(result.getCommandId())) {
                            resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1, null)
                                    .thenAccept(x -> {
                                        publishDomainEventAsync(context.getProcessingCommand(), result);
                                        future.complete(null);
                                    });
                        } else {
                            //如果不是同一个command，则认为是两个不同的command重复创建ID相同的聚合根，我们需要记录错误日志，然后通知当前command的处理完成；
                            String errorMessage = String.format("Duplicate aggregate creation. current commandId:%s, existing commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                                    context.getProcessingCommand().getMessage().getId(),
                                    result.getCommandId(),
                                    result.getAggregateRootId(),
                                    result.getAggregateRootTypeName());
                            logger.error(errorMessage);
                            resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1, null)
                                    .thenAccept(x -> {
                                        CommandResult commandResult = new CommandResult(CommandStatus.Failed, context.getProcessingCommand().getMessage().getId(), context.getEventStream().getAggregateRootId(), "Duplicate aggregate creation.", String.class.getName());
                                        completeCommand(context.getProcessingCommand(), commandResult)
                                                .thenAccept(c -> future.complete(null));
                                    });
                        }
                    } else {
                        String errorMessage = String.format("Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore. commandId:%s, aggregateRootId:%s, aggregateRootTypeName:%s",
                                context.getEventStream().getCommandId(),
                                context.getEventStream().getAggregateRootId(),
                                context.getEventStream().getAggregateRootTypeName());
                        logger.error(errorMessage);
                        resetCommandMailBoxConsumingSequence(context, context.getProcessingCommand().getSequence() + 1, null).thenAccept(x -> {
                            CommandResult commandResult = new CommandResult(CommandStatus.Failed, context.getProcessingCommand().getMessage().getId(), context.getEventStream().getAggregateRootId(), "Duplicate aggregate creation, but we cannot find the existing eventstream from eventstore.", String.class.getName());
                            completeCommand(context.getProcessingCommand(), commandResult)
                                    .thenAccept(c -> future.complete(null));
                        });
                    }
                },
                () -> String.format("[eventStream:%s]", context.getEventStream()),
                null, retryTimes, true);
        return future;
    }

    private void publishDomainEventAsync(ProcessingCommand processingCommand, DomainEventStreamMessage eventStream, int retryTimes) {
        IOHelper.tryAsyncActionRecursivelyWithoutResult("PublishDomainEventAsync",
                () -> domainEventPublisher.publishAsync(eventStream),
                result -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Publish domain events success, {}", eventStream);
                    }
                    String commandHandleResult = processingCommand.getCommandExecuteContext().getResult();
                    CommandResult commandResult = new CommandResult(CommandStatus.Success, processingCommand.getMessage().getId(), eventStream.getAggregateRootId(), commandHandleResult, String.class.getName());
                    completeCommand(processingCommand, commandResult);
                },
                () -> String.format("[eventStream:%s]", eventStream),
                null, retryTimes, true);
    }

    private CompletableFuture<Void> completeCommand(ProcessingCommand processingCommand, CommandResult commandResult) {
        return processingCommand.getMailBox().completeMessage(processingCommand, commandResult);
    }
}