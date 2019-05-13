package com.enode.rocketmq.message;

import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.alibaba.rocketmq.remoting.exception.RemotingException;
import com.enode.common.io.AsyncTaskResult;
import com.enode.common.io.AsyncTaskStatus;
import com.enode.common.logging.ENodeLogger;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SendRocketMQService {

    private static Logger logger = ENodeLogger.getLog();

    public static CompletableFuture<AsyncTaskResult> sendMessageAsync(DefaultMQProducer producer, Message message, String routingKey) {

        CompletableFuture<AsyncTaskResult> promise = new CompletableFuture<>();
        try {
            producer.send(message, SendRocketMQService::messageQueueSelect, routingKey, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    promise.complete(AsyncTaskResult.Success);
                }

                @Override
                public void onException(Throwable ex) {
                    promise.complete(new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage()));
                    logger.error("send callback RocketMQ msg failed, msg: {}", message, ex);
                }
            });
        } catch (MQClientException | RemotingException | InterruptedException e) {
            promise.complete(new AsyncTaskResult(AsyncTaskStatus.IOException, e.getMessage()));
            logger.error("send RocketMQ msg failed, msg: {}", message, e);
        }
        return promise;
    }

    private static MessageQueue messageQueueSelect(List<MessageQueue> queues, Message msg, Object routingKey) {
        int hash = Math.abs(routingKey.hashCode());
        return queues.get(hash % queues.size());
    }
}
