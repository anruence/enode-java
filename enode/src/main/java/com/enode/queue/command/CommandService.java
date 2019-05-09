package com.enode.queue.command;

import com.enode.commanding.CommandResult;
import com.enode.commanding.CommandReturnType;
import com.enode.commanding.ICommand;
import com.enode.commanding.ICommandRoutingKeyProvider;
import com.enode.commanding.ICommandService;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.remoting.common.RemotingUtil;
import com.enode.common.serializing.IJsonSerializer;
import com.enode.common.utilities.Ensure;
import com.enode.queue.QueueMessage;
import com.enode.queue.QueueMessageTypeCode;
import com.enode.queue.TopicData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.CompletableFuture;

public abstract class CommandService implements ICommandService {

    @Autowired
    protected IJsonSerializer _jsonSerializer;

    @Autowired
    protected ICommandRoutingKeyProvider _commandRouteKeyProvider;

    @Autowired
    protected CommandResultProcessor _commandResultProcessor;
    protected TopicData topicData;

    public TopicData getTopicData() {
        return topicData;
    }

    public void setTopicData(TopicData topicData) {
        this.topicData = topicData;
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
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    @Override
    public CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command) {
        return executeAsync(command, CommandReturnType.CommandExecuted);
    }

    @Override
    public CompletableFuture<AsyncTaskResult<CommandResult>> executeAsync(ICommand command, CommandReturnType commandReturnType) {
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }

    protected QueueMessage buildCommandMessage(ICommand command, boolean needReply) {
        Ensure.notNull(command.getAggregateRootId(), "aggregateRootId");
        String commandData = _jsonSerializer.serialize(command);
        String replyAddress = needReply && _commandResultProcessor != null ? RemotingUtil.parseAddress(_commandResultProcessor.getBindingAddress()) : null;
        String messageData = _jsonSerializer.serialize(new CommandMessage(commandData, replyAddress, command.getClass().getName()));
        //命令唯一id，聚合根id
        String key = String.format("%s%s", command.id(), command.getAggregateRootId() == null ? "" : "cmd_agg_" + command.getAggregateRootId());
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(messageData);
        queueMessage.setRouteKey(_commandRouteKeyProvider.getRoutingKey(command));
        queueMessage.setCode(QueueMessageTypeCode.CommandMessage.getValue());
        queueMessage.setKey(key);
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTag());
        return queueMessage;
    }
}
