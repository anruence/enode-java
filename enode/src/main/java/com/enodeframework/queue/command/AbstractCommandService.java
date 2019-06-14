package com.enodeframework.queue.command;

import com.enodeframework.commanding.ICommand;
import com.enodeframework.commanding.ICommandRoutingKeyProvider;
import com.enodeframework.commanding.ICommandService;
import com.enodeframework.common.remoting.common.RemotingUtil;
import com.enodeframework.common.serializing.IJsonSerializer;
import com.enodeframework.common.utilities.Ensure;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.QueueMessageTypeCode;
import com.enodeframework.queue.TopicData;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class AbstractCommandService implements ICommandService {

    @Autowired
    protected IJsonSerializer jsonSerializer;

    @Autowired
    protected ICommandRoutingKeyProvider commandRouteKeyProvider;

    @Autowired
    protected CommandResultProcessor commandResultProcessor;

    protected TopicData topicData;

    public TopicData getTopicData() {
        return topicData;
    }

    public void setTopicData(TopicData topicData) {
        this.topicData = topicData;
    }

    protected QueueMessage buildCommandMessage(ICommand command, boolean needReply) {
        Ensure.notNull(command.getAggregateRootId(), "aggregateRootId");
        String commandData = jsonSerializer.serialize(command);
        String replyAddress = needReply && commandResultProcessor != null ? RemotingUtil.parseAddress(commandResultProcessor.getBindAddress()) : null;
        String messageData = jsonSerializer.serialize(new CommandMessage(commandData, replyAddress, command.getClass().getName()));
        //命令唯一id，聚合根id
        String key = String.format("%s%s", command.id(), command.getAggregateRootId() == null ? "" : "cmd_agg_" + command.getAggregateRootId());
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(messageData);
        queueMessage.setRouteKey(commandRouteKeyProvider.getRoutingKey(command));
        queueMessage.setCode(QueueMessageTypeCode.CommandMessage.getValue());
        queueMessage.setKey(key);
        queueMessage.setTopic(topicData.getTopic());
        queueMessage.setTags(topicData.getTags());
        return queueMessage;
    }
}
