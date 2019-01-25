package com.enode.kafka;

import com.enode.common.io.AsyncTaskResult;
import com.enode.common.logging.ENodeLogger;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class SendMessageService {

    private static Logger logger = ENodeLogger.getLog();

    public CompletableFuture<AsyncTaskResult> sendMessageAsync(Producer producer, ProducerRecord record) {
        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                logger.error("send message failed. topic:{}, metadata:{}", metadata.topic(), metadata.toString(), exception);
            }
        });
        return CompletableFuture.completedFuture(AsyncTaskResult.Success);
    }
}
