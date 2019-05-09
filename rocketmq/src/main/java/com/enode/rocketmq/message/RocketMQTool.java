package com.enode.rocketmq.message;

import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.common.utilities.BitConverter;
import com.enode.queue.QueueMessage;

import java.util.List;

public class RocketMQTool {

    public static QueueMessage covertToQueueMessage(List<MessageExt> msg) {
        MessageExt messageExt = msg.get(0);
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(BitConverter.toString(messageExt.getBody()));
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setKey(messageExt.getKeys());
        queueMessage.setTags(messageExt.getTags());
        queueMessage.setCode(messageExt.getFlag());
        return queueMessage;
    }

    public static Message covertToProducerRecord(QueueMessage queueMessage) {
        return new Message(queueMessage.getTopic(), queueMessage.getTags(), queueMessage.getKey(), queueMessage.getCode(), BitConverter.getBytes(queueMessage.getBody()), true);
    }
}
