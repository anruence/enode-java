package org.enodeframework.commanding.impl;

import com.google.common.base.Strings;
import org.enodeframework.messaging.IApplicationMessage;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandStatus;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandExecuteContext;
import org.enodeframework.commanding.ICommandHandlerProvider;
import org.enodeframework.commanding.ICommandHandlerProxy;
import org.enodeframework.commanding.IProcessingCommandHandler;
import org.enodeframework.commanding.ProcessingCommand;
import org.enodeframework.common.io.IOHelper;
import org.enodeframework.common.io.Task;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Linq;
import org.enodeframework.domain.IAggregateRoot;
import org.enodeframework.domain.IMemoryCache;
import org.enodeframework.eventing.DomainEventStream;
import org.enodeframework.eventing.EventCommittingContext;
import org.enodeframework.eventing.IDomainEvent;
import org.enodeframework.eventing.IEventCommittingService;
import org.enodeframework.eventing.IEventStore;
import org.enodeframework.infrastructure.IObjectProxy;
import org.enodeframework.infrastructure.ITypeNameProvider;
import org.enodeframework.messaging.IMessagePublisher;
import org.enodeframework.messaging.MessageHandlerData;
import org.enodeframework.domain.IDomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author anruence@gmail.com
 */
public class DefaultProcessingCommandHandler implements IProcessingCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(DefaultProcessingCommandHandler.class);
    @Autowired
    private IEventStore eventStore;
    @Autowired
    private ICommandHandlerProvider commandHandlerProvider;
    @Autowired
    private ITypeNameProvider typeNameProvider;
    @Autowired
    private IEventCommittingService eventService;
    @Autowired
    private IMemoryCache memoryCache;
    @Autowired
    private IMessagePublisher<IApplicationMessage> applicationMessagePublisher;
    @Autowired
    private IMessagePublisher<IDomainException> exceptionPublisher;

    public DefaultProcessingCommandHandler setEventStore(IEventStore eventStore) {
        this.eventStore = eventStore;
        return this;
    }

    public DefaultProcessingCommandHandler setCommandHandlerProvider(ICommandHandlerProvider commandHandlerProvider) {
        this.commandHandlerProvider = commandHandlerProvider;
        return this;
    }

    public DefaultProcessingCommandHandler setTypeNameProvider(ITypeNameProvider typeNameProvider) {
        this.typeNameProvider = typeNameProvider;
        return this;
    }

    public DefaultProcessingCommandHandler setEventService(IEventCommittingService eventService) {
        this.eventService = eventService;
        return this;
    }

    public DefaultProcessingCommandHandler setMemoryCache(IMemoryCache memoryCache) {
        this.memoryCache = memoryCache;
        return this;
    }

    public DefaultProcessingCommandHandler setApplicationMessagePublisher(IMessagePublisher<IApplicationMessage> applicationMessagePublisher) {
        this.applicationMessagePublisher = applicationMessagePublisher;
        return this;
    }

    public DefaultProcessingCommandHandler setExceptionPublisher(IMessagePublisher<IDomainException> exceptionPublisher) {
        this.exceptionPublisher = exceptionPublisher;
        return this;
    }

    @Override
    public CompletableFuture<Void> handleAsync(ProcessingCommand processingCommand) {
        ICommand command = processingCommand.getMessage();
        if (Strings.isNullOrEmpty(command.getAggregateRootId())) {
            String errorMessage = String.format("The aggregateRootId of command cannot be null or empty. commandType:%s, commandId:%s", command.getClass().getName(), command.getId());
            logger.error(errorMessage);
            return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), errorMessage);
        }
        HandlerFindResult<ICommandHandlerProxy> findResult = getCommandHandler(processingCommand, commandType -> commandHandlerProvider.getHandlers(commandType));
        if (findResult.getFindStatus() == HandlerFindStatus.Found) {
            return handleCommandInternal(processingCommand, findResult.getFindHandler(), 0);
        } else if (findResult.getFindStatus() == HandlerFindStatus.TooManyHandlerData) {
            logger.error("Found more than one command handler data, commandType:{}, commandId:{}", command.getClass().getName(), command.getId());
            return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), "More than one command handler data found.");
        } else if (findResult.getFindStatus() == HandlerFindStatus.TooManyHandler) {
            logger.error("Found more than one command handler, commandType:{}, commandId:{}", command.getClass().getName(), command.getId());
            return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), "More than one command handler found.");
        } else if (findResult.getFindStatus() == HandlerFindStatus.NotFound) {
            String errorMessage = String.format("No command handler found of command. commandType:%s, commandId:%s", command.getClass().getName(), command.getId());
            logger.error(errorMessage);
            return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), errorMessage);
        }
        return Task.completedTask;
    }

    private CompletableFuture<Void> handleCommandInternal(ProcessingCommand processingCommand, ICommandHandlerProxy commandHandler, int retryTimes) {
        ICommand command = processingCommand.getMessage();
        ICommandExecuteContext commandContext = processingCommand.getCommandExecuteContext();

        CompletableFuture<Void> taskSource = new CompletableFuture<>();
        commandContext.clear();

        IOHelper.tryAsyncActionRecursivelyWithoutResult("HandleCommandAsync",
                () -> {
                    return commandHandler.handleAsync(commandContext, command);
                },
                result -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Handle command success. handlerType:{}, commandType:{}, commandId:{}, aggregateRootId:{}",
                                commandHandler.getInnerObject().getClass().getName(),
                                command.getClass().getName(),
                                command.getId(),
                                command.getAggregateRootId());
                    }
                    if (commandContext.getApplicationMessage() != null) {
                        commitChangesAsync(processingCommand, true, commandContext.getApplicationMessage(), null)
                                .thenAccept(x -> taskSource.complete(null));
                    } else {
                        commitAggregateChanges(processingCommand).thenAccept(x -> {
                            taskSource.complete(null);
                        }).exceptionally(ex -> {
                            logger.error("Commit aggregate changes has unknown exception, handlerType:{}, commandType:{}, commandId:{}, aggregateRootId:{}",
                                    commandHandler.getInnerObject().getClass().getName(),
                                    command.getClass().getName(),
                                    command.getId(),
                                    command.getAggregateRootId(), ex);
                            completeCommand(processingCommand, CommandStatus.Failed, ex.getClass().getName(), "Unknown exception caught when committing changes of command.").thenAccept(x -> {
                                taskSource.complete(null);
                            });
                            return null;
                        });
                    }
                },
                () -> String.format("[command:[id:%s,type:%s],handlerType:%s,aggregateRootId:%s]", command.getId(), command.getClass().getName(), commandHandler.getInnerObject().getClass().getName(), command.getAggregateRootId()),
                (ex, errorMessage) -> {
                    handleExceptionAsync(processingCommand, commandHandler, ex, errorMessage, 0)
                            .thenAccept(x -> taskSource.complete(null));
                }, retryTimes);

        return taskSource;
    }


    private CompletableFuture<Void> commitAggregateChanges(ProcessingCommand processingCommand) {
        ICommand command = processingCommand.getMessage();
        ICommandExecuteContext context = processingCommand.getCommandExecuteContext();
        List<IAggregateRoot> trackedAggregateRoots = context.getTrackedAggregateRoots();
        int dirtyAggregateRootCount = 0;
        IAggregateRoot dirtyAggregateRoot = null;
        List<IDomainEvent> changedEvents = new ArrayList<>();
        for (IAggregateRoot aggregateRoot : trackedAggregateRoots) {
            List<IDomainEvent> events = aggregateRoot.getChanges();
            if (events.size() > 0) {
                dirtyAggregateRootCount++;
                if (dirtyAggregateRootCount > 1) {
                    String errorMessage = String.format("Detected more than one aggregate created or modified by command. commandType:%s, commandId:%s",
                            command.getClass().getName(),
                            command.getId());
                    logger.error(errorMessage);
                    return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), errorMessage);
                }
                dirtyAggregateRoot = aggregateRoot;
                changedEvents = events;
            }
        }
        //如果当前command没有对任何聚合根做修改，框架仍然需要尝试获取该command之前是否有产生事件，
        //如果有，则需要将事件再次发布到MQ；如果没有，则完成命令，返回command的结果为NothingChanged。
        //之所以要这样做是因为有可能当前command上次执行的结果可能是事件持久化完成，但是发布到MQ未完成，然后那时正好机器断电宕机了；
        //这种情况下，如果机器重启，当前command对应的聚合根从eventstore恢复的聚合根是被当前command处理过后的；
        //所以如果该command再次被处理，可能对应的聚合根就不会再产生事件了；
        //所以，我们要考虑到这种情况，尝试再次发布该命令产生的事件到MQ；
        //否则，如果我们直接将当前command设置为完成，即对MQ进行ack操作，那该command的事件就永远不会再发布到MQ了，这样就无法保证CQRS数据的最终一致性了。
        if (dirtyAggregateRootCount == 0 || changedEvents.size() == 0) {
            return processIfNoEventsOfCommand(processingCommand, 0);
        }
        //接受聚合根的最新修改
        dirtyAggregateRoot.acceptChanges();
        DomainEventStream eventStream = new DomainEventStream(
                processingCommand.getMessage().getId(),
                dirtyAggregateRoot.getUniqueId(),
                typeNameProvider.getTypeName(dirtyAggregateRoot.getClass()),
                new Date(),
                changedEvents,
                command.getItems());
        EventCommittingContext committingContext = new EventCommittingContext(dirtyAggregateRoot, eventStream, processingCommand);
        //刷新聚合根的内存缓存
        return memoryCache.updateAggregateRootCache(dirtyAggregateRoot).thenAccept(x -> {
            //构造出一个事件流对象
            //构造出一个事件流对象
            String commandResult = processingCommand.getCommandExecuteContext().getResult();
            if (commandResult != null) {
                processingCommand.getItems().put("CommandResult", commandResult);
            }
            //异步将事件流提交到EventStore
            eventService.commitDomainEventAsync(committingContext);
        });
    }

    private CompletableFuture<Void> processIfNoEventsOfCommand(ProcessingCommand processingCommand, int retryTimes) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        ICommand command = processingCommand.getMessage();
        IOHelper.tryAsyncActionRecursively("ProcessIfNoEventsOfCommand",
                () -> eventStore.findAsync(command.getAggregateRootId(), command.getId()),
                result -> {
                    DomainEventStream existingEventStream = result;
                    if (existingEventStream != null) {
                        eventService.publishDomainEventAsync(processingCommand, existingEventStream);
                        future.complete(null);
                    } else {
                        completeCommand(processingCommand, CommandStatus.NothingChanged, String.class.getName(), processingCommand.getCommandExecuteContext().getResult())
                                .thenAccept(x -> future.complete(null));
                    }
                },
                () -> String.format("[commandId:%s]", command.getId()),
                null, retryTimes, true);
        return future;
    }

    private CompletableFuture<Void> handleExceptionAsync(ProcessingCommand processingCommand, ICommandHandlerProxy commandHandler, Throwable exception, String errorMessage, int retryTimes) {
        ICommand command = processingCommand.getMessage();
        CompletableFuture<Void> future = new CompletableFuture<>();
        IOHelper.tryAsyncActionRecursively("FindEventByCommandIdAsync",
                () -> eventStore.findAsync(command.getAggregateRootId(), command.getId()),
                result -> {
                    DomainEventStream existingEventStream = result;
                    if (existingEventStream != null) {
                        //这里，我们需要再重新做一遍发布事件这个操作；
                        //之所以要这样做是因为虽然该command产生的事件已经持久化成功，但并不表示事件已经发布出去了；
                        //因为有可能事件持久化成功了，但那时正好机器断电了，则发布事件就没有做；
                        eventService.publishDomainEventAsync(processingCommand, existingEventStream);
                        future.complete(null);
                    } else {
                        //到这里，说明当前command执行遇到异常，然后当前command之前也没执行过，是第一次被执行。
                        //那就判断当前异常是否是需要被发布出去的异常，如果是，则发布该异常给所有消费者；
                        //否则，就记录错误日志，然后认为该command处理失败即可；
                        IDomainException domainException = tryGetDomainException(exception);
                        if (domainException != null) {
                            publishExceptionAsync(processingCommand, domainException, 0)
                                    .thenAccept(x -> future.complete(null));
                        } else {
                            completeCommand(processingCommand, CommandStatus.Failed, exception.getClass().getName(), exception.getMessage())
                                    .thenAccept(x -> future.complete(null));
                        }
                    }
                },
                () -> String.format("[command:[id:%s,type:%s],handlerType:%s,aggregateRootId:%s]", command.getId(), command.getClass().getName(), commandHandler.getInnerObject().getClass().getName(), command.getAggregateRootId()),
                null, retryTimes, true
        );
        return future;
    }

    private IDomainException tryGetDomainException(Throwable exception) {
        if (exception == null) {
            return null;
        } else if (exception instanceof IDomainException) {
            return (IDomainException) exception;
        } else if (exception instanceof CompletionException) {
            CompletionException completionException = (CompletionException) exception;
            return (IDomainException) Arrays.stream(completionException.getSuppressed())
                    .filter(x -> x instanceof IDomainException)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    private CompletableFuture<Void> publishExceptionAsync(ProcessingCommand processingCommand, IDomainException exception, int retryTimes) {
        exception.mergeItems(processingCommand.getMessage().getItems());
        CompletableFuture<Void> future = new CompletableFuture<>();
        IOHelper.tryAsyncActionRecursivelyWithoutResult("PublishExceptionAsync",
                () -> exceptionPublisher.publishAsync(exception),
                result -> completeCommand(processingCommand, CommandStatus.Failed, exception.getClass().getName(), ((Exception) exception).getMessage())
                        .thenAccept(x -> future.complete(null)),
                () -> {
                    Map<String, String> serializableInfo = new HashMap<>();
                    exception.serializeTo(serializableInfo);
                    String exceptionInfo = serializableInfo.entrySet().stream().map(x -> String.format("%s:%s", x.getKey(), x.getValue())).collect(Collectors.joining(","));
                    return String.format("[commandId:%s, exceptionInfo:%s]", processingCommand.getMessage().getId(), exceptionInfo);
                },
                null, retryTimes, true);

        return future;
    }

    private CompletableFuture<Void> commitChangesAsync(ProcessingCommand processingCommand, boolean success, IApplicationMessage message, String errorMessage) {
        if (success) {
            if (message != null) {
                message.mergeItems(processingCommand.getMessage().getItems());
                return publishMessageAsync(processingCommand, message, 0);
            } else {
                return completeCommand(processingCommand, CommandStatus.Success, null, null);
            }
        } else {
            return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), errorMessage);
        }
    }

    private CompletableFuture<Void> publishMessageAsync(ProcessingCommand processingCommand, IApplicationMessage message, int retryTimes) {
        ICommand command = processingCommand.getMessage();
        CompletableFuture<Void> future = new CompletableFuture<>();
        IOHelper.tryAsyncActionRecursivelyWithoutResult("PublishApplicationMessageAsync",
                () -> applicationMessagePublisher.publishAsync(message),
                result -> completeCommand(processingCommand, CommandStatus.Success, message.getClass().getName(), JsonTool.serialize(message))
                        .thenAccept(x -> future.complete(null)),
                () -> String.format("[application message:[id:%s,type:%s],command:[id:%s,type:%s]]", message.getId(), message.getClass().getName(), command.getId(), command.getClass().getName()),
                null,
                retryTimes, true);

        return future;
    }

    private <T extends IObjectProxy> HandlerFindResult<T> getCommandHandler(ProcessingCommand processingCommand, Function<Class, List<MessageHandlerData<T>>> getHandlersFunc) {
        ICommand command = processingCommand.getMessage();
        List<MessageHandlerData<T>> handlerDataList = getHandlersFunc.apply(command.getClass());
        if (handlerDataList == null || handlerDataList.size() == 0) {
            return HandlerFindResult.NotFound;
        } else if (handlerDataList.size() > 1) {
            return HandlerFindResult.TooManyHandlerData;
        }
        MessageHandlerData<T> handlerData = Linq.first(handlerDataList);
        if (handlerData.listHandlers == null || handlerData.listHandlers.size() == 0) {
            return HandlerFindResult.NotFound;
        } else if (handlerData.listHandlers.size() > 1) {
            return HandlerFindResult.TooManyHandler;
        }
        T handlerProxy = Linq.first(handlerData.listHandlers);
        return new HandlerFindResult<>(HandlerFindStatus.Found, handlerProxy);
    }

    private CompletableFuture<Void> completeCommand(ProcessingCommand processingCommand, CommandStatus commandStatus, String resultType, String result) {
        CommandResult commandResult = new CommandResult(commandStatus, processingCommand.getMessage().getId(), processingCommand.getMessage().getAggregateRootId(), result, resultType);
        return processingCommand.getMailBox().completeMessage(processingCommand, commandResult);
    }

    enum HandlerFindStatus {
        NotFound,
        Found,
        TooManyHandlerData,
        TooManyHandler
    }

    static class HandlerFindResult<T extends IObjectProxy> {
        static HandlerFindResult NotFound = new HandlerFindResult<>(HandlerFindStatus.NotFound);
        static HandlerFindResult TooManyHandlerData = new HandlerFindResult<>(HandlerFindStatus.TooManyHandlerData);
        static HandlerFindResult TooManyHandler = new HandlerFindResult<>(HandlerFindStatus.TooManyHandler);
        private HandlerFindStatus findStatus;
        private T findHandler;

        HandlerFindResult(HandlerFindStatus findStatus) {
            this(findStatus, null);
        }

        public HandlerFindResult(HandlerFindStatus findStatus, T findHandler) {
            this.findStatus = findStatus;
            this.findHandler = findHandler;
        }

        public HandlerFindStatus getFindStatus() {
            return findStatus;
        }

        public void setFindStatus(HandlerFindStatus findStatus) {
            this.findStatus = findStatus;
        }

        public T getFindHandler() {
            return findHandler;
        }

        public void setFindHandler(T findHandler) {
            this.findHandler = findHandler;
        }
    }
}
