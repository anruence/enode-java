package org.enodeframework.samples.eventhandlers;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.enodeframework.kafka.KafkaApplicationMessageListener;
import org.enodeframework.kafka.KafkaApplicationMessagePublisher;
import org.enodeframework.kafka.KafkaCommandService;
import org.enodeframework.kafka.KafkaDomainEventListener;
import org.enodeframework.kafka.KafkaDomainEventPublisher;
import org.enodeframework.kafka.KafkaPublishableExceptionListener;
import org.enodeframework.kafka.KafkaPublishableExceptionPublisher;
import org.enodeframework.samples.QueueProperties;
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

public class KafkaEventConfig {
    @Bean
    public KafkaCommandService kafkaCommandService(KafkaTemplate kafkaTemplate) {
        KafkaCommandService kafkaCommandService = new KafkaCommandService();
        kafkaCommandService.setProducer(kafkaTemplate);
        kafkaCommandService.setTopic(QueueProperties.COMMAND_TOPIC);
        return kafkaCommandService;
    }

    /**
     * 根据consumerProps填写的参数创建消费者工厂
     */
    @Bean
    public ConsumerFactory consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, QueueProperties.KAFKA_SERVER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, QueueProperties.DEFAULT_PRODUCER_GROUP);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 根据senderProps填写的参数创建生产者工厂
     */
    @Bean
    public ProducerFactory<Object, Object> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, QueueProperties.KAFKA_SERVER);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public RetryTemplate retryTemplate() {
        return new RetryTemplate();
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
    public KafkaMessageListenerContainer domainEventListenerContainer(KafkaDomainEventListener domainEventListener, RetryTemplate retryTemplate) {
        ContainerProperties properties = new ContainerProperties(QueueProperties.EVENT_TOPIC);
        properties.setGroupId(QueueProperties.DEFAULT_PRODUCER_GROUP);
        RetryingMessageListenerAdapter listenerAdapter = new RetryingMessageListenerAdapter(domainEventListener, retryTemplate);
        properties.setMessageListener(listenerAdapter);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public KafkaMessageListenerContainer applicationMessageListenerContainer(KafkaApplicationMessageListener applicationMessageListener, RetryTemplate retryTemplate) {
        ContainerProperties properties = new ContainerProperties(QueueProperties.APPLICATION_TOPIC);
        properties.setGroupId(QueueProperties.DEFAULT_PRODUCER_GROUP);
        RetryingMessageListenerAdapter listenerAdapter = new RetryingMessageListenerAdapter(applicationMessageListener, retryTemplate);
        properties.setMessageListener(listenerAdapter);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public KafkaMessageListenerContainer publishableExceptionListenerContainer(KafkaPublishableExceptionListener publishableExceptionListener, RetryTemplate retryTemplate) {
        ContainerProperties properties = new ContainerProperties(QueueProperties.EXCEPTION_TOPIC);
        properties.setGroupId(QueueProperties.DEFAULT_PRODUCER_GROUP);
        RetryingMessageListenerAdapter listenerAdapter = new RetryingMessageListenerAdapter(publishableExceptionListener, retryTemplate);
        properties.setMessageListener(listenerAdapter);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory(), properties);
    }

    @Bean
    public KafkaApplicationMessagePublisher kafkaApplicationMessagePublisher(KafkaTemplate kafkaTemplate) {
        KafkaApplicationMessagePublisher applicationMessagePublisher = new KafkaApplicationMessagePublisher();
        applicationMessagePublisher.setProducer(kafkaTemplate);
        applicationMessagePublisher.setTopic(QueueProperties.APPLICATION_TOPIC);
        return applicationMessagePublisher;
    }

    @Bean
    public KafkaPublishableExceptionPublisher kafkaPublishableExceptionPublisher(KafkaTemplate kafkaTemplate) {
        KafkaPublishableExceptionPublisher exceptionPublisher = new KafkaPublishableExceptionPublisher();
        exceptionPublisher.setProducer(kafkaTemplate);
        exceptionPublisher.setTopic(QueueProperties.EXCEPTION_TOPIC);
        return exceptionPublisher;
    }

    @Bean
    public KafkaDomainEventPublisher kafkaDomainEventPublisher(KafkaTemplate kafkaTemplate) {
        KafkaDomainEventPublisher domainEventPublisher = new KafkaDomainEventPublisher();
        domainEventPublisher.setProducer(kafkaTemplate);
        domainEventPublisher.setTopic(QueueProperties.EVENT_TOPIC);
        return domainEventPublisher;
    }
}
