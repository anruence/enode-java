package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import org.enodeframework.common.utilities.BitConverter;
import org.enodeframework.queue.QueueMessage;

/**
 * @author anruence@gmail.com
 */
public class OnsTool {

    public static QueueMessage covertToQueueMessage(Message messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(BitConverter.toString(messageExt.getBody()));
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setRouteKey(messageExt.getShardingKey());
        queueMessage.setKey(messageExt.getKey());
        return queueMessage;
    }

    public static Message covertToProducerRecord(QueueMessage queueMessage) {
        Message message = new Message(queueMessage.getTopic(), null, queueMessage.getKey(), BitConverter.getBytes(queueMessage.getBody()));
        message.setShardingKey(queueMessage.getRouteKey());
        return message;
    }
}
