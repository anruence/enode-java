package com.enode.rocketmq.message;

import com.alibaba.rocketmq.common.message.Message;
import com.enode.commanding.CommandResult;
import com.enode.commanding.CommandReturnType;
import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandRoutingKeyProvider;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.utilities.Ensure;
import com.enode.queue.ITopicProvider;
import com.enode.queue.QueueMessage;
import com.enode.queue.command.CommandResultProcessor;
import com.enode.queue.command.CommandService;
import com.enode.rocketmq.client.Producer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.CompletableFuture;

@Singleton
public class RocketMQCommandService extends CommandService {

    private Producer _producer;

    private SendRocketMQService _sendMessageService;

    @Inject
    public RocketMQCommandService(Producer producer, IJsonSerializer jsonSerializer, ITopicProvider<ICommand> commandTopicProvider, CommandResultProcessor commandResultProcessor, ICommandRoutingKeyProvider commandRoutingKeyProvider, SendRocketMQService sendMessageService) {
        _jsonSerializer = jsonSerializer;
        _commandTopicProvider = commandTopicProvider;
        _commandResultProcessor = commandResultProcessor;
        _commandRouteKeyProvider = commandRoutingKeyProvider;
        _sendMessageService = sendMessageService;
        _producer = producer;
    }

    @Override
    public RocketMQCommandService start() {
        super.start();
        return this;
    }

    @Override
    public RocketMQCommandService shutdown() {
        super.shutdown();
        return this;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(ICommand command) {
        try {
            QueueMessage queueMessage = buildCommandMessage(command, false);
            Message message = RocketMQTool.covertToProducerRecord(queueMessage);
            return _sendMessageService.sendMessageAsync(_producer, message, queueMessage.getRouteKey());
        } catch (Exception ex) {
            return CompletableFuture.completedFuture(new AsyncTaskResult<>(AsyncTaskStatus.Failed, ex.getMessage()));
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
            QueueMessage queueMessage = buildCommandMessage(command, true);
            Message message = RocketMQTool.covertToProducerRecord(queueMessage);
            CompletableFuture<AsyncTaskResult> sendMessageAsync = _sendMessageService.sendMessageAsync(_producer, message, queueMessage.getRouteKey());
            sendMessageAsync.thenAccept(sendResult -> {
                if (sendResult.getStatus().equals(AsyncTaskStatus.Success)) {
                    //_commandResultProcessor中会继续等命令或事件处理完成的状态
                } else {
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
