package org.enodeframework.queue.command;

import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.serializing.JsonTool;
import org.enodeframework.common.utilities.Ensure;
import org.enodeframework.common.utilities.RemotingUtil;
import org.enodeframework.queue.QueueMessage;
import org.enodeframework.queue.QueueMessageTypeCode;
import org.enodeframework.queue.TopicData;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractCommandService implements ICommandService {
    @Autowired
    protected CommandResultProcessor commandResultProcessor;

    private TopicData topicData;

    public AbstractCommandService setCommandResultProcessor(CommandResultProcessor commandResultProcessor) {
        this.commandResultProcessor = commandResultProcessor;
        return this;
    }

    public TopicData getTopicData() {
        return topicData;
    }

    public void setTopicData(TopicData topicData) {
        this.topicData = topicData;
    }

    protected QueueMessage buildCommandMessage(ICommand command, boolean needReply) {
        Ensure.notNull(command.getAggregateRootId(), "aggregateRootId");
        Ensure.notNull(topicData, "topicData");
        String commandData = JsonTool.serialize(command);
        String replyAddress = needReply && commandResultProcessor != null ? RemotingUtil.parseAddress(commandResultProcessor.getBindAddress()) : null;
        CommandMessage commandMessage = new CommandMessage();
        commandMessage.setCommandData(commandData);
        commandMessage.setReplyAddress(replyAddress);
        commandMessage.setCommandType(command.getClass().getName());
        String messageData = JsonTool.serialize(commandMessage);
        //命令唯一id，聚合根id
        String key = String.format("%s%s", command.getId(), command.getAggregateRootId() == null ? "" : "cmd_agg_" + command.getAggregateRootId());
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(messageData);
        queueMessage.setRouteKey(command.getAggregateRootId());
        queueMessage.setCode(QueueMessageTypeCode.CommandMessage.getValue());
        queueMessage.setKey(key);
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTags());
        return queueMessage;
    }
}
