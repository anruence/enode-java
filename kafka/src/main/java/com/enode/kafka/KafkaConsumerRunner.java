package com.enode.kafka;

import com.enode.common.logging.ENodeLogger;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class KafkaConsumerRunner<K, V> implements Runnable {
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private static final Logger _logger = ENodeLogger.getLog();

    private KafkaConsumer kafkaConsumer;

    private IMessageListener messageListener;

    KafkaConsumerRunner(KafkaConsumer kafkaConsumer, IMessageListener messageListener) {
        this.kafkaConsumer = kafkaConsumer;
        this.messageListener = messageListener;
    }

    @Override
    public void run() {
        try {
            while (!closed.get()) {
                try {
                    ConsumerRecords<K, V> records = kafkaConsumer.poll(Duration.ofMillis(1000));
                    for (ConsumerRecord record : records) {
                        messageListener.receiveMessage(record, message -> {
                            _logger.info("run something:{}", message.getBody());
                            return;
                        });
                    }
                } catch (Exception e) {
                    _logger.error("consumer message failed", e);
                }
            }
        } catch (WakeupException e) {
            // Ignore exception if closing
            if (!closed.get()) {
                throw e;
            }
        } finally {
            kafkaConsumer.close();
        }
    }

    public void shutdown() {
        closed.set(true);
        kafkaConsumer.wakeup();
    }

}
