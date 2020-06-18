package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class RocketMQCommandListener implements MessageListenerConcurrently {

    private final IMessageHandler commandListener;

    public RocketMQCommandListener(IMessageHandler commandListener) {
        this.commandListener = commandListener;
    }

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        final CountDownLatch latch = new CountDownLatch(msgs.size());
        msgs.forEach(messageExt -> {
            QueueMessage queueMessage = RocketMQTool.covertToQueueMessage(messageExt);
            commandListener.handle(queueMessage, message -> {
                latch.countDown();
            });
        });
        Task.await(latch);
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}
