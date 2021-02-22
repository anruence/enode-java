package org.enodeframework.rocketmq.message;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.selector.SelectMessageQueueByHash;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class RocketMQSendMessageService implements ISendMessageService {

    private static final Logger logger = LoggerFactory.getLogger(RocketMQSendMessageService.class);

    private final MQProducer producer;

    public RocketMQSendMessageService(MQProducer producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<Boolean> sendMessageAsync(QueueMessage queueMessage) {
        CompletableFuture<Boolean> promise = new CompletableFuture<>();
        Message message = RocketMQTool.covertToProducerRecord(queueMessage);
        try {
            producer.send(message, new SelectMessageQueueByHash(), queueMessage.getRouteKey(), new SendCallback() {
                @Override
                public void onSuccess(SendResult result) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Enode message async send success, sendResult: {}, message: {}", result, new String(message.getBody()));
                    }
                    promise.complete(true);
                }

                @Override
                public void onException(Throwable ex) {
                    promise.completeExceptionally(new IORuntimeException(ex));
                    logger.error("Enode message async send has exception, message: {}, routingKey: {}", message, queueMessage.getRouteKey(), ex);
                }
            });
        } catch (MQClientException | RemotingException | InterruptedException ex) {
            promise.completeExceptionally(new IORuntimeException(ex));
            logger.error("Enode message async send has exception, message: {}, routingKey: {}", message, queueMessage.getRouteKey(), ex);
        }
        return promise;
    }
}
