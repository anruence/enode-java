package org.enodeframework.queue.command;

import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.common.utilities.RemotingUtil;
import org.enodeframework.queue.QueueMessage;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCommandService implements ICommandService {

    @Autowired
    protected CommandResultProcessor commandResultProcessor;

    public AbstractCommandService setCommandResultProcessor(CommandResultProcessor commandResultProcessor) {
        this.commandResultProcessor = commandResultProcessor;
        return this;
    }

    private String topic;


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

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

}
