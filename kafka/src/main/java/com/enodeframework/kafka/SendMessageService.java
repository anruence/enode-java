package com.enodeframework.kafka;

import com.enodeframework.common.io.AsyncTaskResult;
import com.enodeframework.common.io.AsyncTaskStatus;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * @author anruence@gmail.com
 */
public class SendMessageService {

    private static Logger logger = LoggerFactory.getLogger(SendMessageService.class);

    public static CompletableFuture<AsyncTaskResult> sendMessageAsync(KafkaTemplate<String, String> producer, ProducerRecord<String, String> record) {
        CompletableFuture<AsyncTaskResult> future = new CompletableFuture<>();
        producer.send(record).completable().whenComplete((r, e) -> {
            if (e != null) {
                future.complete(new AsyncTaskResult(AsyncTaskStatus.IOException));
                logger.error("send kafka record error, record:{}", record, e);
                return;
            }
            future.complete(AsyncTaskResult.Success);
        });
        return future;
    }
}
