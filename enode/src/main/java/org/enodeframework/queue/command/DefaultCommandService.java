package org.enodeframework.queue.command;

import org.enodeframework.commanding.CommandResult;
import org.enodeframework.commanding.CommandReturnType;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.common.utilities.RemotingUtil;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.QueueMessage;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class DefaultCommandService implements ICommandService {

    private final String topic;

    private final ISendMessageService producer;

    private final ICommandResultProcessor commandResultProcessor;

    public DefaultCommandService(String topic, ICommandResultProcessor commandResultProcessor, ISendMessageService producer) {
        this.topic = topic;
        this.commandResultProcessor = commandResultProcessor;
        this.producer = producer;
    }

    @Override
    public CompletableFuture<Void> sendAsync(ICommand command) {
        return producer.sendMessageAsync(buildCommandMessage(command, false));
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
            CompletableFuture<Void> sendMessageAsync = producer.sendMessageAsync(buildCommandMessage(command, true));
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

    protected QueueMessage buildCommandMessage(ICommand command, boolean needReply) {
        Ensure.notNull(command.getAggregateRootId(), "aggregateRootId");
        Ensure.notNull(topic, "topic");
        String commandData = JsonTool.serialize(command);
        String replyAddress = needReply && commandResultProcessor != null ? RemotingUtil.parseAddress(commandResultProcessor.getBindAddress()) : null;
        CommandMessage commandMessage = new CommandMessage();
        commandMessage.setCommandData(commandData);
        commandMessage.setReplyAddress(replyAddress);
        commandMessage.setCommandType(command.getClass().getName());
        String messageData = JsonTool.serialize(commandMessage);
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(messageData);
        queueMessage.setRouteKey(command.getAggregateRootId());
        String key = String.format("%s%s", command.getId(), command.getAggregateRootId() == null ? "" : "_cmd_agg_" + command.getAggregateRootId());
        queueMessage.setKey(key);
        queueMessage.setTopic(topic);
        return queueMessage;
    }
}
