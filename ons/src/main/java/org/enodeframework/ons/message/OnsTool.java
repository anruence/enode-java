package org.enodeframework.ons.message;

import com.aliyun.openservices.ons.api.Message;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class OnsTool {

    private static QueueMessage covertToQueueMessage(Message messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(new String(messageExt.getBody(), StandardCharsets.UTF_8));
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setTag(messageExt.getTag());
        queueMessage.setRouteKey(messageExt.getShardingKey());
        queueMessage.setKey(messageExt.getKey());
        return queueMessage;
    }

    public static Message covertToProducerRecord(QueueMessage queueMessage) {
        Message message = new Message(queueMessage.getTopic(), queueMessage.getTag(), queueMessage.getKey(), queueMessage.getBody().getBytes(StandardCharsets.UTF_8));
        message.setShardingKey(queueMessage.getRouteKey());
        return message;
    }

    public static void handle(List<Message> msgs, IMessageHandler messageHandler) {
        int size = msgs.size();
        CountDownLatch latch = new CountDownLatch(size);
        handleConcurrently(0, size, msgs, latch, messageHandler);
        Task.await(latch);
    }

    private static void handleConcurrently(int index, int total, List<Message> msgs, CountDownLatch latch, IMessageHandler messageHandler) {
        msgs.forEach(msg -> {
            QueueMessage queueMessage = covertToQueueMessage(msg);
            messageHandler.handle(queueMessage, message -> {
                latch.countDown();
            });
        });
    }

    private static void handleRecursively(int index, int total, List<Message> msgs, CountDownLatch latch, IMessageHandler messageHandler) {
        if (index == total) {
            return;
        }
        QueueMessage queueMessage = covertToQueueMessage(msgs.get(index));
        messageHandler.handle(queueMessage, message -> {
            latch.countDown();
            handleRecursively(index + 1, total, msgs, latch, messageHandler);
        });
    }
}
