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
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
public class SendRocketMQService {

    private static Logger logger = ENodeLogger.getLog();

    public CompletableFuture<AsyncTaskResult> sendMessageAsync(DefaultMQProducer producer, Message message, String routingKey) {

        CompletableFuture promise = new CompletableFuture();
        try {
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
        } catch (MQClientException e) {
            e.printStackTrace();
        } catch (RemotingException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return promise;
    }

    private MessageQueue messageQueueSelect(List<MessageQueue> queues, Message msg, Object routingKey) {
        int hash = Math.abs(routingKey.hashCode());
        return queues.get(hash % queues.size());
    }
}
