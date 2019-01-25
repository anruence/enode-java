package com.enode.rocketmq.message;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.common.logging.ENodeLogger;
import com.enode.rocketmq.client.Producer;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SendRocketMQService {

    private static Logger logger = ENodeLogger.getLog();

    public CompletableFuture<AsyncTaskResult> sendMessageAsync(Producer producer, Message message, String routingKey) {

        CompletableFuture promise = new CompletableFuture();
        producer.send(message, this::messageQueueSelect, routingKey, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                promise.complete(AsyncTaskResult.Success);
            }

            @Override
            public void onException(Throwable ex) {
                promise.complete(new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage()));
            }
        });

        return promise;
    }

    private MessageQueue messageQueueSelect(List<MessageQueue> queues, Message msg, Object routingKey) {
        int hash = Math.abs(routingKey.hashCode());
        return queues.get(hash % queues.size());
    }
}
