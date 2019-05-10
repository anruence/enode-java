package com.enode.kafka;

import com.enode.commanding.CommandResult;
import com.enode.commanding.CommandReturnType;
import com.enode.commanding.ICommand;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.common.utilities.Ensure;
import com.enode.queue.QueueMessage;
import com.enode.queue.command.CommandService;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public class KafkaCommandService extends CommandService {

    private KafkaProducer producer;
    @Autowired
    private SendMessageService _sendMessageService;

    public KafkaProducer getProducer() {
        return producer;
    }

    public void setProducer(KafkaProducer producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<AsyncTaskResult> sendAsync(ICommand command) {
        try {
            return _sendMessageService.sendMessageAsync(producer, buildKafkaMessage(command, false));
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

            CompletableFuture<AsyncTaskResult> sendMessageAsync = _sendMessageService.sendMessageAsync(producer, buildKafkaMessage(command, true));
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


    protected ProducerRecord buildKafkaMessage(ICommand command, boolean needReply) {
        QueueMessage queueMessage = buildCommandMessage(command, needReply);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }

}
