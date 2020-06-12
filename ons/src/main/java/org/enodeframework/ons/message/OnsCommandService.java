package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.Producer;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.command.AbstractCommandService;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class OnsCommandService extends AbstractCommandService {

    private Producer producer;

    @Override
    public CompletableFuture<Void> sendAsync(ICommand command) {
        QueueMessage queueMessage = buildCommandMessage(command, false);
        Message message = OnsTool.covertToProducerRecord(queueMessage);
        return SendOnsService.sendMessageAsync(producer, message);
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync(ICommand command) {
        return executeAsync(command, CommandReturnType.CommandExecuted);
    }

    @Override
    public CompletableFuture<CommandResult> executeAsync(ICommand command, CommandReturnType commandReturnType) {
        CompletableFuture<CommandResult> taskCompletionSource = new CompletableFuture<>();
        try {
            Ensure.notNull(commandResultProcessor, "commandResultProcessor");
            commandResultProcessor.registerProcessingCommand(command, commandReturnType, taskCompletionSource);
            QueueMessage queueMessage = buildCommandMessage(command, true);
            Message message = OnsTool.covertToProducerRecord(queueMessage);
            CompletableFuture<Void> sendMessageAsync = SendOnsService.sendMessageAsync(producer, message);
            sendMessageAsync.thenAccept(sendResult -> {
            }).exceptionally(ex -> {
                commandResultProcessor.processFailedSendingCommand(command);
                taskCompletionSource.completeExceptionally(ex);
                return null;
            });
        } catch (Exception ex) {
            taskCompletionSource.completeExceptionally(ex);
        }
        return taskCompletionSource;
    }

    public Producer getProducer() {
        return producer;
    }

    public void setProducer(Producer producer) {
        this.producer = producer;
    }
}
