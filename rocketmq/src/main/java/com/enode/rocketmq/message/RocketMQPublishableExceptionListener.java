package com.enode.rocketmq.message;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.queue.QueueMessage;
import com.enode.queue.publishableexceptions.PublishableExceptionListener;

import java.util.List;

public class RocketMQPublishableExceptionListener extends PublishableExceptionListener implements MessageListenerConcurrently {

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        QueueMessage queueMessage = RocketMQTool.covertToQueueMessage(msgs);
        handle(queueMessage, message -> {
        });
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
