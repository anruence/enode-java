package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
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
public class RocketMQCommandService extends AbstractCommandService {

    private DefaultMQProducer defaultMQProducer;

    @Override
    public CompletableFuture<Void> sendAsync(ICommand command) {
        QueueMessage queueMessage = buildCommandMessage(command, false);
        Message message = RocketMQTool.covertToProducerRecord(queueMessage);
        return SendRocketMQService.sendMessageAsync(defaultMQProducer, message, queueMessage.getRouteKey());
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
            Message message = RocketMQTool.covertToProducerRecord(queueMessage);
            CompletableFuture<Void> sendMessageAsync = SendRocketMQService.sendMessageAsync(defaultMQProducer, message, queueMessage.getRouteKey());
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

    public DefaultMQProducer getDefaultMQProducer() {
        return defaultMQProducer;
    }

    public void setDefaultMQProducer(DefaultMQProducer defaultMQProducer) {
        this.defaultMQProducer = defaultMQProducer;
    }
}
