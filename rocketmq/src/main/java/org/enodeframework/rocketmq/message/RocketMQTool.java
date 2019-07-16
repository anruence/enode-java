package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.enodeframework.queue.QueueMessage;

import java.nio.charset.StandardCharsets;

/**
 * @author anruence@gmail.com
 */
public class RocketMQTool {
    public static QueueMessage covertToQueueMessage(MessageExt messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(new String(messageExt.getBody(), StandardCharsets.UTF_8));
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setTag(messageExt.getTags());
        queueMessage.setKey(messageExt.getKeys());
        return queueMessage;
    }

    public static Message covertToProducerRecord(QueueMessage queueMessage) {
        return new Message(queueMessage.getTopic(), queueMessage.getTag(), queueMessage.getKey(), queueMessage.getBody().getBytes(StandardCharsets.UTF_8));
    }
}
