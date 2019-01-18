package com.qianzhui.enode.kafka;

import com.alibaba.rocketmq.common.message.Message;
import com.qianzhui.enode.common.io.AsyncTaskResult;
import com.qianzhui.enode.common.logging.ENodeLogger;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class SendKafkaService {

    private static final Logger logger = ENodeLogger.getLog();

    public CompletableFuture<AsyncTaskResult> sendMessageAsync(Producer producer, Message message, String routingKey, String messageId, String version) {
        CompletableFuture<AsyncTaskResult> promise = new CompletableFuture<>();
        logger.info("============= send kafka message, keys:{}, messageid: {},routingKey: {}", message.getKeys(), messageId, routingKey);
        ProducerRecord record = new ProducerRecord("", "");
        producer.send(record, (metadata, exception) -> {

        });
        return promise;
    }
}
