package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;

import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class OnsPublishableExceptionListener implements MessageListener {

    private final IMessageHandler publishableExceptionListener;

    public OnsPublishableExceptionListener(IMessageHandler publishableExceptionListener) {
        this.publishableExceptionListener = publishableExceptionListener;
    }

    @Override
    public Action consume(Message message, ConsumeContext context) {
        final CountDownLatch latch = new CountDownLatch(1);
        QueueMessage queueMessage = OnsTool.covertToQueueMessage(message);
        publishableExceptionListener.handle(queueMessage, m -> {
            latch.countDown();
        });
        Task.await(latch);
        return Action.CommitMessage;
    }
}
