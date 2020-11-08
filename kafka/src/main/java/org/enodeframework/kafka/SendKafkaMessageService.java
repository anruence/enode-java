package org.enodeframework.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.enodeframework.common.exception.IORuntimeException;
import org.enodeframework.queue.ISendMessageService;
import org.enodeframework.queue.QueueMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class SendKafkaMessageService implements ISendMessageService {
    private final static Logger logger = LoggerFactory.getLogger(SendKafkaMessageService.class);

    private final KafkaTemplate<String, String> producer;

    public SendKafkaMessageService(KafkaTemplate<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public CompletableFuture<Boolean> sendMessageAsync(QueueMessage queueMessage) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        ProducerRecord<String, String> message = KafkaTool.covertToProducerRecord(queueMessage);
        producer.send(message).addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable throwable) {
                logger.error("Enode message async send has exception, message: {}", message, throwable);
                future.completeExceptionally(new IORuntimeException(throwable));
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Enode message async send success, sendResult: {}, message: {}", result, message);
                }
                future.complete(true);
            }
        });
        return future;
    }
}
