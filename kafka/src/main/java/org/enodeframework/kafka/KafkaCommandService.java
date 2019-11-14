package org.enodeframework.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.command.AbstractCommandService;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class KafkaCommandService extends AbstractCommandService {
    private KafkaTemplate<String, String> producer;

    public KafkaTemplate<String, String> getProducer() {
        return producer;
    }

    public void setProducer(KafkaTemplate<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<Void> sendAsync(ICommand command) {
        return SendMessageService.sendMessageAsync(producer, buildKafkaMessage(command, false));
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
            CompletableFuture<Void> sendMessageAsync = SendMessageService.sendMessageAsync(producer, buildKafkaMessage(command, true));
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

    protected ProducerRecord<String, String> buildKafkaMessage(ICommand command, boolean needReply) {
        QueueMessage queueMessage = buildCommandMessage(command, needReply);
        return KafkaTool.covertToProducerRecord(queueMessage);
    }
}
