package org.enodeframework.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.enodeframework.common.exception.IORuntimeException;
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
    private final static Logger logger = LoggerFactory.getLogger(SendMessageService.class);

    public static CompletableFuture<Void> sendMessageAsync(KafkaTemplate<String, String> producer, ProducerRecord<String, String> message) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        producer.send(message).addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                logger.error("ENode message async send has exception, message: {}", message, throwable);
                future.completeExceptionally(new IORuntimeException(throwable));
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                if (logger.isDebugEnabled()) {
                    logger.debug("ENode message async send success, sendResult: {}, message: {}", result, message);
                }
                future.complete(null);
            }
        });
        return future;
    }
}
