package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.enodeframework.common.utilities.BitConverter;
import org.enodeframework.queue.QueueMessage;

/**
 * @author anruence@gmail.com
 */
public class RocketMQTool {
    public static QueueMessage covertToQueueMessage(MessageExt messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(BitConverter.toString(messageExt.getBody()));
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setKey(messageExt.getKeys());
        return queueMessage;
    }

    public static Message covertToProducerRecord(QueueMessage queueMessage) {
        return new Message(queueMessage.getTopic(), null, queueMessage.getKey(), BitConverter.getBytes(queueMessage.getBody()));
    }
}
