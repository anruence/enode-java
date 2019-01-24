package com.enode.samples.note.controller;

import com.enode.ENode;
import com.enode.commanding.ICommandService;
import com.enode.kafka.KafkaConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class AppConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public KafkaConfig kafkaConfig() {

        ENode enode = ENode.create("com.enode.samples").registerDefaultComponents();

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "100");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", "localhost:9092");
        producerProps.put("enable.idempotence", "true");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaConfig config = new KafkaConfig(enode);
        config.useKafka(producerProps, props, ENode.PUBLISHERS, 6000);
        return config;
    }

    @Bean
    public ICommandService commandService() {
        return kafkaConfig().getEnode().getContainer().resolve(ICommandService.class);
    }
}
