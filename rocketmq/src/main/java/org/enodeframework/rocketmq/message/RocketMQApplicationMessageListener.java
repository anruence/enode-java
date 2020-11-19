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
public class RocketMQApplicationMessageListener implements MessageListenerOrderly {

    private final IMessageHandler applicationMessageListener;

    public RocketMQApplicationMessageListener(IMessageHandler applicationMessageListener) {
        this.applicationMessageListener = applicationMessageListener;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        RocketMQTool.handle(msgs, applicationMessageListener);
        return ConsumeOrderlyStatus.SUCCESS;
    }
}