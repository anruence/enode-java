package com.enodeframework.rocketmq.message;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.applicationmessage.AbstractApplicationMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class RocketMQApplicationMessageListener extends AbstractApplicationMessageListener implements MessageListenerConcurrently {

    private final Logger LOGGER = LoggerFactory.getLogger(RocketMQApplicationMessageListener.class);

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        QueueMessage queueMessage = RocketMQTool.covertToQueueMessage(msgs);
        try {
            handle(queueMessage, message -> {
            });
        } catch (Exception e) {
            LOGGER.error("ApplicationMessage consumer failed, queueMessage:{}", queueMessage);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
