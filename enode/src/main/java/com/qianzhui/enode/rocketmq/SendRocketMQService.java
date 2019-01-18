package com.qianzhui.enode.rocketmq;

import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.client.producer.SendResult;
import com.alibaba.rocketmq.client.producer.SendStatus;
import com.alibaba.rocketmq.common.message.Message;
import com.alibaba.rocketmq.common.message.MessageQueue;
import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.common.io.AsyncTaskStatus;
import com.qianzhui.enode.common.io.IOHelper;
import com.qianzhui.enode.common.logging.ENodeLogger;
import com.qianzhui.enode.rocketmq.client.Producer;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SendRocketMQService {
    private static final Logger logger = ENodeLogger.getLog();

    private IOHelper ioHelper;

    @Inject
    public SendRocketMQService(IOHelper _ioHelper) {
        ioHelper = _ioHelper;
    }

    public void sendMessage(Producer producer, Message message, String routingKey, String messageId, String version) {

        try {
            SendResult result = producer.send(message, this::messageQueueSelect, routingKey);
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                logger.error("ENode message sync send failed, sendResult: {}, routingKey: {}, messageId: {}, version: {}", result, routingKey, messageId, version);
            } else {
                logger.info("ENode message sync send success, sendResult: {}, routingKey: {}, messageId: {}, version: {}", result, routingKey, messageId, version);
            }
        } catch (Exception ex) {
            logger.error(String.format("ENode message sync send has exception, message: {}, routingKey: {}, messageId: {}, version: {}", message, routingKey, messageId, version), ex);
            throw ex;
        }
    }

    public CompletableFuture<AsyncTaskResult> sendMessageAsync(Producer producer, Message message, String routingKey, String messageId, String version) {
        CompletableFuture<AsyncTaskResult> promise = new CompletableFuture<>();
        logger.info("============= send rocketmq message,keys:{},messageid: {},routingKey: {}", message.getKeys(), messageId, routingKey);
        try {
            producer.send(message, this::messageQueueSelect, routingKey, new SendCallback() {
                @Override
                public void onSuccess(SendResult sendResult) {
                    logger.info("ENode message async send success, keys:{}, sendResult: {}, routingKey: {}, messageId: {}, version: {}", message.getKeys(), sendResult, routingKey, messageId, version);
                    promise.complete(AsyncTaskResult.Success);
                }

                @Override
                public void onException(Throwable ex) {
                    logger.error("ENode message async send failed, keys:{}, routingKey: {}, messageId: {}, version: {}", message.getKeys(), routingKey, messageId, version);
                    promise.complete(new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage()));
                }
            });
        } catch (Exception ex) {
            logger.error(String.format("ENode message async send has exception,keys:%s,message: %s, routingKey: %s, messageId: %s, version: %s", message.getKeys(), message, routingKey, messageId, version), ex);
            promise.complete(new AsyncTaskResult(AsyncTaskStatus.IOException, ex.getMessage()));
        }

        return promise;
    }


    private MessageQueue messageQueueSelect(List<MessageQueue> queues, Message msg, Object routingKey) {
        int hash = Math.abs(routingKey.hashCode());
        return queues.get(hash % queues.size());
    }
}
