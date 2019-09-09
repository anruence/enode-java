package org.enodeframework.samples.commandhandles;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.enodeframework.kafka.KafkaApplicationMessagePublisher;
import org.enodeframework.kafka.KafkaCommandListener;
import org.enodeframework.kafka.KafkaDomainEventPublisher;
import org.enodeframework.kafka.KafkaPublishableExceptionPublisher;
import org.enodeframework.queue.TopicData;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.adapter.RetryingMessageListenerAdapter;
import org.springframework.retry.support.RetryTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.enodeframework.samples.QueueProperties.APPLICATION_TOPIC;
import static org.enodeframework.samples.QueueProperties.COMMAND_TOPIC;
import static org.enodeframework.samples.QueueProperties.DEFAULT_CONSUMER_GROUP;
import static org.enodeframework.samples.QueueProperties.EVENT_TOPIC;
import static org.enodeframework.samples.QueueProperties.EXCEPTION_TOPIC;
import static org.enodeframework.samples.QueueProperties.KAFKA_SERVER;

public class KafkaCommandConfig {
    @Bean
    public KafkaCommandListener commandListener() {
        return new KafkaCommandListener();
    }

    @Bean
    public RetryTemplate retryTemplate() {
        return new RetryTemplate();
    }

    @Bean
    public KafkaMessageListenerContainer kafkaMessageListenerContainer(KafkaCommandListener commandListener, RetryTemplate retryTemplate) {
        ContainerProperties properties = new ContainerProperties(COMMAND_TOPIC);
        properties.setGroupId(DEFAULT_CONSUMER_GROUP);
        RetryingMessageListenerAdapter listenerAdapter = new RetryingMessageListenerAdapter(commandListener, retryTemplate);
        properties.setMessageListener(listenerAdapter);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public KafkaDomainEventPublisher kafkaDomainEventPublisher(KafkaTemplate kafkaTemplate) {
        KafkaDomainEventPublisher domainEventPublisher = new KafkaDomainEventPublisher();
        domainEventPublisher.setProducer(kafkaTemplate);
        domainEventPublisher.setTopicData(new TopicData(EVENT_TOPIC, "*"));
        return domainEventPublisher;
    }

    /**
     * 应用消息生产者，复用生产者实例发送到不同topic中
     */
    @Bean
    public KafkaApplicationMessagePublisher kafkaApplicationMessagePublisher(KafkaTemplate kafkaTemplate) {
        KafkaApplicationMessagePublisher applicationMessagePublisher = new KafkaApplicationMessagePublisher();
        applicationMessagePublisher.setProducer(kafkaTemplate);
        applicationMessagePublisher.setTopicData(new TopicData(APPLICATION_TOPIC, "*"));
        return applicationMessagePublisher;
    }

    /**
     * 异常消息生产者，复用生产者实例发送到不同topic中
     */
    @Bean
    public KafkaPublishableExceptionPublisher kafkaPublishableExceptionPublisher(KafkaTemplate kafkaTemplate) {
        KafkaPublishableExceptionPublisher exceptionPublisher = new KafkaPublishableExceptionPublisher();
        exceptionPublisher.setProducer(kafkaTemplate);
        exceptionPublisher.setTopicData(new TopicData(EXCEPTION_TOPIC, "*"));
        return exceptionPublisher;
    }

    /**
     * 根据consumerProps填写的参数创建消费者工厂
     */
    @Bean
    public ConsumerFactory consumerFactory() {
        // 消费者配置参数
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_SERVER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, DEFAULT_CONSUMER_GROUP);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 根据senderProps填写的参数创建生产者工厂
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
