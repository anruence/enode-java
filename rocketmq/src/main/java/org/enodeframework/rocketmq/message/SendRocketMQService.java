package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.enodeframework.common.exception.IORuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class SendRocketMQService {

    private static final Logger logger = LoggerFactory.getLogger(SendRocketMQService.class);

    public static CompletableFuture<Void> sendMessageAsync(MQProducer producer, Message message, String routingKey) {
        CompletableFuture<Void> promise = new CompletableFuture<>();
        try {
            producer.send(message, new SelectMessageQueueByHash(), routingKey, new SendCallback() {
                @Override
                public void onSuccess(SendResult result) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("ENode message async send success, sendResult: {}, message: {}", result, message);
                    }
                    promise.complete(null);
                }

                @Override
                public void onException(Throwable ex) {
                    promise.completeExceptionally(new IORuntimeException(ex));
                    logger.error("ENode message async send has exception, message: {}, routingKey: {}", message, routingKey, ex);
                }
            });
        } catch (MQClientException | RemotingException | InterruptedException ex) {
            promise.completeExceptionally(new IORuntimeException(ex));
            logger.error("ENode message async send has exception, message: {}, routingKey: {}", message, routingKey, ex);
        }
        return promise;
    }
}
