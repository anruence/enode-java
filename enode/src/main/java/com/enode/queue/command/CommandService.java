package com.enode.queue.command;

import com.enode.commanding.CommandExecuteTimeoutException;
import com.enode.commanding.CommandResult;
import com.enode.commanding.CommandReturnType;
import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandRoutingKeyProvider;
import com.enode.commanding.ICommandService;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.common.utilities.Ensure;
import com.enode.infrastructure.WrappedRuntimeException;
import com.enode.queue.IMQProducer;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CommandService implements ICommandService {

    private ICommandRoutingKeyProvider _commandRouteKeyProvider;
    private IMQProducer _sendMessageService;
    private CommandResultProcessor _commandResultProcessor;

    @Inject
    public CommandService(
            CommandResultProcessor commandResultProcessor,
            ICommandRoutingKeyProvider commandRoutingKeyProvider,
            IMQProducer sendQueueMessageService) {
        super();
        _commandRouteKeyProvider = commandRoutingKeyProvider;
        _commandResultProcessor = commandResultProcessor;
        _sendMessageService = sendQueueMessageService;
    }

    public CommandService start() {
        if (_commandResultProcessor != null) {
            _commandResultProcessor.start();
        }
        return this;
    }

    public CommandService shutdown() {
        if (_commandResultProcessor != null) {
            _commandResultProcessor.shutdown();
        }
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(ICommand command) {
        try {
            return _sendMessageService.sendAsync(command, _commandRouteKeyProvider.getRoutingKey(command), false);
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage()));
        }
    }

    public CommandResult execute(ICommand command, int timeoutMillis) {
        try {
            AsyncTaskResult<CommandResult> result = executeAsync(command).get(timeoutMillis, TimeUnit.MILLISECONDS);
            return result.getData();
        } catch (TimeoutException e) {
            throw new CommandExecuteTimeoutException(String.format("Command execute timeout, commandId: %s, aggregateRootId: %s", command.id(), command.getAggregateRootId()));
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    public CommandResult execute(ICommand command, CommandReturnType commandReturnType, int timeoutMillis) {
        try {
            AsyncTaskResult<CommandResult> result = executeAsync(command, commandReturnType).get(timeoutMillis, TimeUnit.MILLISECONDS);
            return result.getData();
        } catch (TimeoutException e) {
            throw new CommandExecuteTimeoutException(String.format("Command execute timeout, commandId: %s, aggregateRootId: %s", command.id(), command.getAggregateRootId()));
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command) {
        return executeAsync(command, CommandReturnType.CommandExecuted);
    }

    @Override
    public CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command, CommandReturnType commandReturnType) {
        try {
            Ensure.notNull(_commandResultProcessor, "commandResultProcessor");

            CompletableFuture<AsyncTaskResult<CommandResult>> taskCompletionSource = new CompletableFuture<>();
            _commandResultProcessor.registerProcessingCommand(command, commandReturnType, taskCompletionSource);

            CompletableFuture<AsyncTaskResult> sendMessageAsync = _sendMessageService.sendAsync(command, _commandRouteKeyProvider.getRoutingKey(command), true);
            sendMessageAsync.thenAccept(sendResult -> {
                if (sendResult.getStatus().equals(AsyncTaskStatus.Success)) {
                    //_commandResultProcessor中会继续等命令或事件处理完成的状态
                } else {
                    //TODO 是否删除下面一行代码
                    taskCompletionSource.complete(new AsyncTaskResult<>(sendResult.getStatus(), sendResult.getErrorMessage()));
                    _commandResultProcessor.processFailedSendingCommand(command);
                }
            });

            return taskCompletionSource;
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage()));
        }
    }


}
