package com.enodeframework.rocketmq.message;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class SendRocketMQService {
    private static Logger logger = LoggerFactory.getLogger(SendRocketMQService.class);

    public static CompletableFuture<AsyncTaskResult> sendMessageAsync(DefaultMQProducer producer, Message message, String routingKey) {
        CompletableFuture<AsyncTaskResult> promise = new CompletableFuture<>();
        try {
            producer.send(message, SendRocketMQService::messageQueueSelect, routingKey, new SendCallback() {
                @Override
                public void onSuccess(SendResult result) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("ENode message async send success, sendResult: {}, message: {}", result, message);
                    }
                    promise.complete(AsyncTaskResult.Success);
                }

                @Override
                public void onException(Throwable ex) {
                    promise.complete(new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage()));
                    logger.error("ENode message async send has exception, message: {}, routingKey: {}", message, routingKey, ex);
                }
            });
        } catch (MQClientException | RemotingException | InterruptedException ex) {
            promise.complete(new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage()));
            logger.error("ENode message async send has exception, message: {}, routingKey: {}", message, routingKey, ex);
        }
        return promise;
    }

    private static MessageQueue messageQueueSelect(List<MessageQueue> queues, Message msg, Object routingKey) {
        int hash = Math.abs(routingKey.hashCode());
        return queues.get(hash % queues.size());
    }
}
