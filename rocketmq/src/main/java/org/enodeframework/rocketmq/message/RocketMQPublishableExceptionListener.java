package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.enodeframework.queue.IMessageHandler;

import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class RocketMQPublishableExceptionListener implements MessageListenerOrderly {

    private final IMessageHandler publishableExceptionListener;

    public RocketMQPublishableExceptionListener(IMessageHandler publishableExceptionListener) {
        this.publishableExceptionListener = publishableExceptionListener;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        RocketMQTool.handle(msgs, publishableExceptionListener);
        return ConsumeOrderlyStatus.SUCCESS;
    }
}
