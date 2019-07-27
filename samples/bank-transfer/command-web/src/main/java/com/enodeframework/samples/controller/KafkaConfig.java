package com.enodeframework.samples.controller;

import com.enodeframework.kafka.KafkaCommandService;
import com.enodeframework.queue.TopicData;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.enodeframework.samples.QueueProperties.COMMAND_TOPIC;
import static com.enodeframework.samples.QueueProperties.KAFKA_SERVER;

public class KafkaConfig {
    @Bean
    public KafkaCommandService kafkaCommandService(KafkaTemplate producer) {
        KafkaCommandService kafkaCommandService = new KafkaCommandService();
        kafkaCommandService.setProducer(producer);
        TopicData topicData = new TopicData(COMMAND_TOPIC, "*");
        kafkaCommandService.setTopicData(topicData);
        return kafkaCommandService;
    }

    /**
     * 根据senderProps的参数创建生产者工厂
     */
    @Bean
    public ProducerFactory producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    /**
     * kafkaTemplate实现了Kafka发送接收等功能
     */
    @Bean
    public KafkaTemplate kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
