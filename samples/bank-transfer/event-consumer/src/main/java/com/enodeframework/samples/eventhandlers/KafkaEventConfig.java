package com.enodeframework.samples.eventhandlers;

import com.enodeframework.kafka.KafkaApplicationMessageListener;
import com.enodeframework.kafka.KafkaApplicationMessagePublisher;
import com.enodeframework.kafka.KafkaCommandService;
import com.enodeframework.kafka.KafkaDomainEventListener;
import com.enodeframework.kafka.KafkaDomainEventPublisher;
import com.enodeframework.kafka.KafkaPublishableExceptionListener;
import com.enodeframework.kafka.KafkaPublishableExceptionPublisher;
import com.enodeframework.queue.TopicData;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

import static com.enodeframework.samples.QueueProperties.APPLICATION_TOPIC;
import static com.enodeframework.samples.QueueProperties.COMMAND_TOPIC;
import static com.enodeframework.samples.QueueProperties.DEFAULT_PRODUCER_GROUP;
import static com.enodeframework.samples.QueueProperties.EVENT_TOPIC;
import static com.enodeframework.samples.QueueProperties.EXCEPTION_TOPIC;
import static com.enodeframework.samples.QueueProperties.KAFKA_SERVER;

public class KafkaEventConfig {

    @Bean
    public KafkaCommandService kafkaCommandService(KafkaTemplate kafkaTemplate) {
        KafkaCommandService kafkaCommandService = new KafkaCommandService();
        kafkaCommandService.setProducer(kafkaTemplate);
        TopicData topicData = new TopicData(COMMAND_TOPIC, "*");
        kafkaCommandService.setTopicData(topicData);
        return kafkaCommandService;
    }

    /**
     * 根据consumerProps填写的参数创建消费者工厂
     */
    @Bean
    public ConsumerFactory consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        //连接地址
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        //GroupID
        props.put(ConsumerConfig.GROUP_ID_CONFIG, DEFAULT_PRODUCER_GROUP);
        //是否自动提交
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        //自动提交的频率
//        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        //Session超时设置
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        //键的反序列化方式
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        //值的反序列化方式
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 根据senderProps填写的参数创建生产者工厂
     */
    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
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


    @Bean
    public KafkaPublishableExceptionListener publishableExceptionListener() {
        return new KafkaPublishableExceptionListener();
    }

    @Bean
    public KafkaApplicationMessageListener applicationMessageListener() {
        return new KafkaApplicationMessageListener();
    }

    @Bean
    public KafkaDomainEventListener domainEventListener() {
        return new KafkaDomainEventListener();
    }


    @Bean
    public KafkaMessageListenerContainer domainEventListenerContainer(KafkaDomainEventListener domainEventListener) {
        ContainerProperties properties = new ContainerProperties(EVENT_TOPIC);
        properties.setGroupId(DEFAULT_PRODUCER_GROUP);
        properties.setMessageListener(domainEventListener);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public KafkaMessageListenerContainer applicationMessageListenerContainer(KafkaApplicationMessageListener applicationMessageListener) {
        ContainerProperties properties = new ContainerProperties(APPLICATION_TOPIC);
        properties.setGroupId(DEFAULT_PRODUCER_GROUP);
        properties.setMessageListener(applicationMessageListener);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public KafkaMessageListenerContainer publishableExceptionListenerContainer(KafkaPublishableExceptionListener publishableExceptionListener) {
        ContainerProperties properties = new ContainerProperties(EXCEPTION_TOPIC);
        properties.setGroupId(DEFAULT_PRODUCER_GROUP);
        properties.setMessageListener(publishableExceptionListener);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public KafkaApplicationMessagePublisher kafkaApplicationMessagePublisher(KafkaTemplate kafkaTemplate) {
        KafkaApplicationMessagePublisher applicationMessagePublisher = new KafkaApplicationMessagePublisher();
        applicationMessagePublisher.setProducer(kafkaTemplate);
        applicationMessagePublisher.setTopicData(new TopicData(APPLICATION_TOPIC, "*"));
        return applicationMessagePublisher;
    }

    @Bean
    public KafkaPublishableExceptionPublisher kafkaPublishableExceptionPublisher(KafkaTemplate kafkaTemplate) {
        KafkaPublishableExceptionPublisher exceptionPublisher = new KafkaPublishableExceptionPublisher();
        exceptionPublisher.setProducer(kafkaTemplate);
        exceptionPublisher.setTopicData(new TopicData(EXCEPTION_TOPIC, "*"));
        return exceptionPublisher;
    }

    @Bean
    public KafkaDomainEventPublisher kafkaDomainEventPublisher(KafkaTemplate kafkaTemplate) {
        KafkaDomainEventPublisher domainEventPublisher = new KafkaDomainEventPublisher();
        domainEventPublisher.setProducer(kafkaTemplate);
        domainEventPublisher.setTopicData(new TopicData(EVENT_TOPIC, "*"));
        return domainEventPublisher;
    }

}
