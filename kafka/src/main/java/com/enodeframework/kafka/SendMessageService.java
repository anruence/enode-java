package com.enodeframework.kafka;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class SendMessageService {

    private static Logger logger = LoggerFactory.getLogger(SendMessageService.class);

    public static CompletableFuture<AsyncTaskResult> sendMessageAsync(KafkaTemplate<String, String> producer, ProducerRecord<String, String> message) {
        CompletableFuture<AsyncTaskResult> future = new CompletableFuture<>();
        producer.send(message).addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                future.complete(new AsyncTaskResult(AsyncTaskStatus.IOException));
                logger.error("ENode message async send has exception, message: {}, routingKey: {}", message, throwable);
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                if (logger.isDebugEnabled()) {
                    logger.debug("ENode message async send success, sendResult: {}, message: {}", result, message);
                }
                future.complete(AsyncTaskResult.Success);
            }
        });
        return future;
    }
}
