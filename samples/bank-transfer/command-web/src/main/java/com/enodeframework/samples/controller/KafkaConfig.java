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
        //连接地址
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        //重试，0为不启用重试机制
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        //控制批处理大小，单位为字节
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        //批量发送，延迟为1毫秒，启用该功能能有效减少生产者发送消息次数，从而提高并发量
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        //生产者可以使用的总内存字节来缓冲等待发送到服务器的记录
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1024000);
        //键的序列化方式
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        //值的序列化方式
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
