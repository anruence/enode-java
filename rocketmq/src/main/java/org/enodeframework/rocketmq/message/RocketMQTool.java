package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.enodeframework.common.io.Task;
import org.enodeframework.queue.IMessageHandler;
import org.enodeframework.queue.QueueMessage;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author anruence@gmail.com
 */
public class RocketMQTool {
    private static QueueMessage covertToQueueMessage(MessageExt messageExt) {
        QueueMessage queueMessage = new QueueMessage();
        queueMessage.setBody(new String(messageExt.getBody(), StandardCharsets.UTF_8));
        queueMessage.setTopic(messageExt.getTopic());
        queueMessage.setTag(messageExt.getTags());
        queueMessage.setKey(messageExt.getKeys());
        return queueMessage;
    }

    public static Message covertToProducerRecord(QueueMessage queueMessage) {
        Message message = new Message(queueMessage.getTopic(), queueMessage.getTag(), queueMessage.getKey(), queueMessage.getBody().getBytes(StandardCharsets.UTF_8));
        return message;
    }

    public static void handle(List<MessageExt> msgs, IMessageHandler messageHandler) {
        int size = msgs.size();
        CountDownLatch latch = new CountDownLatch(size);
        handleRecursively(0, size, msgs, latch, messageHandler);
        Task.await(latch);
    }

    private static void handleConcurrently(int index, int total, List<MessageExt> msgs, CountDownLatch latch, IMessageHandler messageHandler) {
        msgs.forEach(msg -> {
            QueueMessage queueMessage = covertToQueueMessage(msg);
            messageHandler.handle(queueMessage, message -> {
                latch.countDown();
            });
        });
    }

    private static void handleRecursively(int index, int total, List<MessageExt> msgs, CountDownLatch latch, IMessageHandler messageHandler) {
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
