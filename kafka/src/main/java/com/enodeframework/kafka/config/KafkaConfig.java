package com.enodeframework.kafka.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.ListenableFuture;

public class KafkaConfig {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @KafkaListener(topics = "")
    public void list() {
        ListenableFuture future = kafkaTemplate.send("", "");
        future.completable();
    }
}
