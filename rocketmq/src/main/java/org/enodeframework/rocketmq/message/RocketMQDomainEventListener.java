package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.IMessageHandler;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class RocketMQDomainEventListener implements MessageListenerOrderly {

    private final IMessageHandler domainEventListener;

    public RocketMQDomainEventListener(IMessageHandler domainEventListener) {
        this.domainEventListener = domainEventListener;
    }

    @Override
    public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
        RocketMQTool.handle(msgs, domainEventListener);
        return ConsumeOrderlyStatus.SUCCESS;
    }
}
