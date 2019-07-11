package com.enodeframework.commanding.impl;

import com.enodeframework.commanding.CommandResult;
import com.enodeframework.commanding.CommandStatus;
import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ICommandAsyncHandlerProvider;
import com.enodeframework.commanding.ICommandAsyncHandlerProxy;
import com.enodeframework.commanding.ICommandExecuteContext;
import com.enodeframework.commanding.ICommandHandlerProvider;
import com.enodeframework.commanding.ICommandHandlerProxy;
import com.enodeframework.commanding.IProcessingCommandHandler;
import com.enodeframework.commanding.ProcessingCommand;
import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import com.enodeframework.common.io.IOHelper;
import com.enodeframework.common.io.IORuntimeException;
import com.enodeframework.common.io.Task;
import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.domain.IAggregateRoot;
import com.enodeframework.eventing.DomainEventStream;
import com.enodeframework.eventing.EventCommittingContext;
import com.enodeframework.eventing.IDomainEvent;
import com.enodeframework.eventing.IEventService;
import com.enodeframework.eventing.IEventStore;
import com.enodeframework.infrastructure.IApplicationMessage;
import com.enodeframework.infrastructure.IMessagePublisher;
import com.enodeframework.infrastructure.IObjectProxy;
import com.enodeframework.infrastructure.IPublishableException;
import com.enodeframework.infrastructure.ITypeNameProvider;
import com.enodeframework.infrastructure.MessageHandlerData;
import com.enodeframework.infrastructure.WrappedRuntimeException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.enodeframework.common.io.Task.await;

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
    private ICommandAsyncHandlerProvider commandAsyncHandlerProvider;

    @Autowired
    private ITypeNameProvider typeNameProvider;

    @Autowired
    private IEventService eventService;

    @Autowired
    private IMessagePublisher<IApplicationMessage> applicationMessagePublisher;

    @Autowired
    private IMessagePublisher<IPublishableException> exceptionPublisher;

    @Override
    public CompletableFuture<Void> handle(ProcessingCommand processingCommand) {
        ICommand command = processingCommand.getMessage();
        if (Strings.isNullOrEmpty(command.getAggregateRootId())) {
            String errorMessage = String.format("The aggregateRootId of command cannot be null or empty. commandType:%s, commandId:%s", command.getClass().getName(), command.id());
            logger.error(errorMessage);
            return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), errorMessage);
        }
        HandlerFindResult<ICommandHandlerProxy> findResult = getCommandHandler(processingCommand, commandType -> commandHandlerProvider.getHandlers(commandType));
        if (findResult.getFindStatus() == HandlerFindStatus.Found) {
            return handleCommand(processingCommand, findResult.getFindHandler());
        } else if (findResult.getFindStatus() == HandlerFindStatus.TooManyHandlerData) {
            logger.error("Found more than one command handler data, commandType:{}, commandId:{}", command.getClass().getName(), command.id());
            return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), "More than one command handler data found.");
        } else if (findResult.getFindStatus() == HandlerFindStatus.TooManyHandler) {
            logger.error("Found more than one command handler, commandType:{}, commandId:{}", command.getClass().getName(), command.id());
            return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), "More than one command handler found.");
        } else if (findResult.getFindStatus() == HandlerFindStatus.NotFound) {
            HandlerFindResult<ICommandAsyncHandlerProxy> asyncFindResult = getCommandHandler(processingCommand, commandType -> commandAsyncHandlerProvider.getHandlers(commandType));
            ICommandAsyncHandlerProxy commandAsyncHandler = asyncFindResult.getFindHandler();
            if (asyncFindResult.getFindStatus() == HandlerFindStatus.Found) {
                return handleCommand(processingCommand, commandAsyncHandler);
            } else if (asyncFindResult.getFindStatus() == HandlerFindStatus.TooManyHandlerData) {
                logger.error("Found more than one command async handler data, commandType:{}, commandId:{}", command.getClass().getName(), command.id());
                return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), "More than one command async handler data found.");
            } else if (asyncFindResult.getFindStatus() == HandlerFindStatus.TooManyHandler) {
                logger.error("Found more than one command async handler, commandType:{}, commandId:{}", command.getClass().getName(), command.id());
                return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), "More than one command async handler found.");
            } else if (asyncFindResult.getFindStatus() == HandlerFindStatus.NotFound) {
                String errorMessage = String.format("No command handler found of command. commandType:%s, commandId:%s", command.getClass().getName(), command.id());
                logger.error(errorMessage);
                return completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), errorMessage);
            }
        }
        return Task.CompletedTask;
    }

    private CompletableFuture<Void> handleCommand(ProcessingCommand processingCommand, ICommandHandlerProxy commandHandler) {
        ICommand command = processingCommand.getMessage();
        processingCommand.getCommandExecuteContext().clear();
        //调用command handler执行当前command
        boolean handleSuccess = false;
        try {
            await(commandHandler.handleAsync(processingCommand.getCommandExecuteContext(), command));
            if (logger.isDebugEnabled()) {
                logger.debug("Handle command success. handlerType:{}, commandType:{}, commandId:{}, aggregateRootId:{}",
                        commandHandler.getInnerObject().getClass().getName(),
                        command.getClass().getName(),
                        command.id(),
                        command.getAggregateRootId());
            }
            handleSuccess = true;
        } catch (Exception ex) {
            handleExceptionAsync(processingCommand, commandHandler, ex, 0);
            return Task.CompletedTask;
        }
        //如果command执行成功，则提交执行后的结果
        if (handleSuccess) {
            try {
                commitAggregateChanges(processingCommand);
            } catch (Exception ex) {
                logCommandExecuteException(processingCommand, commandHandler, ex);
                // await
                await(completeCommand(processingCommand, CommandStatus.Failed, ex.getClass().getName(), "Unknown exception caught when committing changes of command."));
            }
        }
        return Task.CompletedTask;
    }

    private void commitAggregateChanges(ProcessingCommand processingCommand) {
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
                            command.id());
                    logger.error(errorMessage);
                    completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), errorMessage);
                    return;
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
            processIfNoEventsOfCommand(processingCommand, 0);
            return;
        }

        //构造出一个事件流对象
        DomainEventStream eventStream = buildDomainEventStream(dirtyAggregateRoot, changedEvents, processingCommand);

        //将事件流提交到EventStore
        eventService.commitDomainEventAsync(new EventCommittingContext(dirtyAggregateRoot, eventStream, processingCommand));
    }

    private void processIfNoEventsOfCommand(ProcessingCommand processingCommand, int retryTimes) {
        ICommand command = processingCommand.getMessage();

        IOHelper.tryAsyncActionRecursively("ProcessIfNoEventsOfCommand",
                () -> eventStore.findAsync(command.getAggregateRootId(), command.id()),
                currentRetryTimes -> processIfNoEventsOfCommand(processingCommand, currentRetryTimes),

                result ->
                {
                    DomainEventStream existingEventStream = result.getData();
                    if (existingEventStream != null) {
                        eventService.publishDomainEventAsync(processingCommand, existingEventStream);
                    } else {
                        completeCommand(processingCommand, CommandStatus.NothingChanged, String.class.getName(), processingCommand.getCommandExecuteContext().getResult());
                    }
                },
                () -> String.format("[commandId:%s]", command.id()),
                errorMessage -> logger.error("Find event by commandId has unknown exception, the code should not be run to here, errorMessage: {}", errorMessage),
                retryTimes, true);
    }

    private DomainEventStream buildDomainEventStream(IAggregateRoot aggregateRoot, List<IDomainEvent> changedEvents, ProcessingCommand processingCommand) {
        String commandResult = processingCommand.getCommandExecuteContext().getResult();
        if (commandResult != null) {
            processingCommand.getItems().put("CommandResult", commandResult);
        }
        return new DomainEventStream(
                processingCommand.getMessage().id(),
                aggregateRoot.uniqueId(),
                typeNameProvider.getTypeName(aggregateRoot.getClass()),
                aggregateRoot.version() + 1,
                new Date(),
                changedEvents,
                processingCommand.getItems());
    }

    private void handleExceptionAsync(ProcessingCommand processingCommand, ICommandHandlerProxy commandHandler, Exception exception, int retryTimes) {
        ICommand command = processingCommand.getMessage();

        IOHelper.tryAsyncActionRecursively("FindEventByCommandIdAsync",
                () -> eventStore.findAsync(command.getAggregateRootId(), command.id()),
                currentRetryTimes -> handleExceptionAsync(processingCommand, commandHandler, exception, currentRetryTimes),

                result -> {
                    DomainEventStream existingEventStream = result.getData();

                    if (existingEventStream != null) {
                        //这里，我们需要再重新做一遍发布事件这个操作；
                        //之所以要这样做是因为虽然该command产生的事件已经持久化成功，但并不表示事件已经发布出去了；
                        //因为有可能事件持久化成功了，但那时正好机器断电了，则发布事件就没有做；
                        logger.info("handle command exception,and the command has consumed before,we will publish domain event again and try execute next command mailbox message.", exception);
                        eventService.publishDomainEventAsync(processingCommand, existingEventStream);
                    } else {
                        //到这里，说明当前command执行遇到异常，然后当前command之前也没执行过，是第一次被执行。
                        //那就判断当前异常是否是需要被发布出去的异常，如果是，则发布该异常给所有消费者；否则，就记录错误日志；
                        //然后，认为该command处理失败即可；

                        Throwable exp = exception;
                        if (exp instanceof WrappedRuntimeException) {
                            exp = ((WrappedRuntimeException) exp).getException();
                        }

                        if (exp instanceof IPublishableException) {
                            IPublishableException publishableException = (IPublishableException) exp;
                            publishExceptionAsync(processingCommand, publishableException, 0);
                        } else {
                            logCommandExecuteException(processingCommand, commandHandler, exp);
                            completeCommand(processingCommand, CommandStatus.Failed, exp.getClass().getName(), exp.getMessage());
                        }
                    }
                },
                () -> String.format("[commandId:%s]", command.id()),
                errorMessage -> logger.error(String.format("Find event by commandId has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true
        );

    }

    private void publishExceptionAsync(ProcessingCommand processingCommand, IPublishableException exception, int retryTimes) {
        IOHelper.tryAsyncActionRecursively("PublishExceptionAsync",
                () -> exceptionPublisher.publishAsync(exception),
                currentRetryTimes -> publishExceptionAsync(processingCommand, exception, currentRetryTimes),
                result -> completeCommand(processingCommand, CommandStatus.Failed, exception.getClass().getName(), ((Exception) exception).getMessage()),
                () -> {
                    Map<String, String> serializableInfo = new HashMap<>();
                    exception.serializeTo(serializableInfo);
                    String exceptionInfo = String.join(",", serializableInfo.entrySet().stream().map(x -> String.format("%s:%s", x.getKey(), x.getValue())).collect(Collectors.toList()));
                    return String.format("[commandId:%s, exceptionInfo:%s]", processingCommand.getMessage().id(), exceptionInfo);
                },
                errorMessage -> logger.error(String.format("Publish event has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
    }

    private void logCommandExecuteException(ProcessingCommand processingCommand, ICommandHandlerProxy commandHandler, Throwable exception) {
        ICommand command = processingCommand.getMessage();
        String errorMessage = String.format("%s raised when %s handling %s. commandId:%s, aggregateRootId:%s",
                exception.getClass().getName(),
                commandHandler.getInnerObject().getClass().getName(),
                command.getClass().getName(),
                command.id(),
                command.getAggregateRootId());
        logger.error(errorMessage, exception);
    }

    private CompletableFuture<Void> handleCommand(ProcessingCommand processingCommand, ICommandAsyncHandlerProxy commandHandler) {
        return handleCommandAsync(processingCommand, commandHandler, 0);
    }

    private CompletableFuture<Void> handleCommandAsync(ProcessingCommand processingCommand, ICommandAsyncHandlerProxy commandHandler, int retryTimes) {
        ICommand command = processingCommand.getMessage();
        IOHelper.tryAsyncActionRecursively("HandleCommandAsync", () -> {
                    try {
                        CompletableFuture<AsyncTaskResult<IApplicationMessage>> asyncResult = commandHandler.handleAsync(command);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Handle command async success. handlerType:{}, commandType:{}, commandId:{}, aggregateRootId:{}",
                                    commandHandler.getInnerObject().getClass().getName(),
                                    command.getClass().getName(),
                                    command.id(),
                                    command.getAggregateRootId());
                        }
                        return asyncResult;
                    } catch (IORuntimeException ex) {
                        logger.error(String.format("Handle command async has io exception. handlerType:%s, commandType:%s, commandId:%s, aggregateRootId:%s",
                                commandHandler.getInnerObject().getClass().getName(),
                                command.getClass().getName(),
                                command.id(),
                                command.getAggregateRootId()), ex);
                        return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.IOException, ex.getMessage()));
                    } catch (Exception ex) {
                        logger.error(String.format("Handle command async has unknown exception. handlerType:%s, commandType:%s, commandId:%s, aggregateRootId:%s",
                                commandHandler.getInnerObject().getClass().getName(),
                                command.getClass().getName(),
                                command.id(),
                                command.getAggregateRootId()), ex);
                        return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage()));
                    }
                },
                currentRetryTimes -> handleCommandAsync(processingCommand, commandHandler, currentRetryTimes),
                result -> commitChangesAsync(processingCommand, true, result.getData(), null),
                () -> String.format("[command:[id:%s,type:%s],handlerType:%s]", command.id(), command.getClass().getName(), commandHandler.getInnerObject().getClass().getName()),
                errorMessage -> commitChangesAsync(processingCommand, false, null, errorMessage),
                retryTimes, false);
        return Task.CompletedTask;
    }

    private void commitChangesAsync(ProcessingCommand processingCommand, boolean success, IApplicationMessage message, String errorMessage) {
        if (success) {
            if (message != null) {
                publishMessageAsync(processingCommand, message, 0);
            } else {
                completeCommand(processingCommand, CommandStatus.Success, null, null);
            }
        } else {
            completeCommand(processingCommand, CommandStatus.Failed, String.class.getName(), errorMessage);
        }
    }

    private void publishMessageAsync(ProcessingCommand processingCommand, IApplicationMessage message, int retryTimes) {
        ICommand command = processingCommand.getMessage();

        IOHelper.tryAsyncActionRecursively("PublishApplicationMessageAsync",
                () -> applicationMessagePublisher.publishAsync(message),
                currentRetryTimes -> publishMessageAsync(processingCommand, message, currentRetryTimes),
                result -> completeCommand(processingCommand, CommandStatus.Success, message.getTypeName(), JsonTool.serialize(message)),
                () -> String.format("[application message:[id:%s,type:%s],command:[id:%s,type:%s]]", message.id(), message.getClass().getName(), command.id(), command.getClass().getName()),
                errorMessage -> logger.error(String.format("Publish application message has unknown exception, the code should not be run to here, errorMessage: %s", errorMessage)),
                retryTimes, true);
    }

    private <T extends IObjectProxy> HandlerFindResult<T> getCommandHandler(ProcessingCommand processingCommand, Function<Class, List<MessageHandlerData<T>>> getHandlersFunc) {
        ICommand command = processingCommand.getMessage();
        List<MessageHandlerData<T>> handlerDataList = getHandlersFunc.apply(command.getClass());

        if (handlerDataList == null || handlerDataList.size() == 0) {
            return HandlerFindResult.NotFound;
        } else if (handlerDataList.size() > 1) {
            return HandlerFindResult.TooManyHandlerData;
        }

        MessageHandlerData<T> handlerData = handlerDataList.get(0);

        if (handlerData.ListHandlers == null || handlerData.ListHandlers.size() == 0) {
            return HandlerFindResult.NotFound;
        } else if (handlerData.ListHandlers.size() > 1) {
            return HandlerFindResult.TooManyHandler;
        }

        T handlerProxy = handlerData.ListHandlers.get(0);

        return new HandlerFindResult<>(HandlerFindStatus.Found, handlerProxy);
    }

    private CompletableFuture<Void> completeCommand(ProcessingCommand processingCommand, CommandStatus commandStatus, String resultType, String result) {
        CommandResult commandResult = new CommandResult(commandStatus, processingCommand.getMessage().id(), processingCommand.getMessage().getAggregateRootId(), result, resultType);
        return processingCommand.getMailbox().completeMessage(processingCommand, commandResult);
    }

    enum HandlerFindStatus {
        /**
         *
         */
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
