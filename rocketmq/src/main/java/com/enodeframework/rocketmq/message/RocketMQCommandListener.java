package com.enodeframework.rocketmq.message;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.command.AbstractCommandListener;

import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class RocketMQCommandListener extends AbstractCommandListener implements MessageListenerConcurrently {
    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        QueueMessage queueMessage = RocketMQTool.covertToQueueMessage(msgs);
        handle(queueMessage, message -> {
        });
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
