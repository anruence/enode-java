package org.enodeframework.tests;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.enodeframework.kafka.KafkaApplicationMessageListener;
import org.enodeframework.kafka.KafkaApplicationMessagePublisher;
import org.enodeframework.kafka.KafkaCommandListener;
import org.enodeframework.kafka.KafkaCommandService;
import org.enodeframework.kafka.KafkaDomainEventListener;
import org.enodeframework.kafka.KafkaDomainEventPublisher;
import org.enodeframework.kafka.KafkaPublishableExceptionListener;
import org.enodeframework.kafka.KafkaPublishableExceptionPublisher;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

@ConditionalOnProperty(prefix = "spring.enode.mq", name = "kafka")
public class KafkaConfig {
    @Bean
    public KafkaCommandService kafkaCommandService(KafkaTemplate kafkaTemplate) {
        KafkaCommandService kafkaCommandService = new KafkaCommandService();
        kafkaCommandService.setProducer(kafkaTemplate);
        kafkaCommandService.setTopic(Constants.COMMAND_TOPIC);
        return kafkaCommandService;
    }

    /**
     * 根据consumerProps填写的参数创建消费者工厂
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_SERVER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.DEFAULT_PRODUCER_GROUP);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "15000");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * 根据senderProps填写的参数创建生产者工厂
     */
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.KAFKA_SERVER);
        props.put(ProducerConfig.RETRIES_CONFIG, 1);
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 1024000);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
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
    public KafkaCommandListener commandListener() {
        return new KafkaCommandListener();
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> commandListenerContainer(KafkaCommandListener commandListener, ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(Constants.COMMAND_TOPIC);
        properties.setGroupId(Constants.DEFAULT_CONSUMER_GROUP);
        properties.setMessageListener(commandListener);
        properties.setMissingTopicsFatal(false);
        return new KafkaMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> domainEventListenerContainer(KafkaDomainEventListener domainEventListener, ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(Constants.EVENT_TOPIC);
        properties.setGroupId(Constants.DEFAULT_PRODUCER_GROUP);
        properties.setMessageListener(domainEventListener);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> applicationMessageListenerContainer(KafkaApplicationMessageListener applicationMessageListener, ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(Constants.APPLICATION_TOPIC);
        properties.setGroupId(Constants.DEFAULT_PRODUCER_GROUP);
        properties.setMessageListener(applicationMessageListener);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    public KafkaMessageListenerContainer<String, String> publishableExceptionListenerContainer(KafkaPublishableExceptionListener publishableExceptionListener, ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(Constants.EXCEPTION_TOPIC);
        properties.setGroupId(Constants.DEFAULT_PRODUCER_GROUP);
        properties.setMessageListener(publishableExceptionListener);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL);
        return new KafkaMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    public KafkaApplicationMessagePublisher kafkaApplicationMessagePublisher(KafkaTemplate<String, String> kafkaTemplate) {
        KafkaApplicationMessagePublisher applicationMessagePublisher = new KafkaApplicationMessagePublisher();
        applicationMessagePublisher.setProducer(kafkaTemplate);
        applicationMessagePublisher.setTopic(Constants.APPLICATION_TOPIC);
        return applicationMessagePublisher;
    }

    @Bean
    public KafkaPublishableExceptionPublisher kafkaPublishableExceptionPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        KafkaPublishableExceptionPublisher exceptionPublisher = new KafkaPublishableExceptionPublisher();
        exceptionPublisher.setProducer(kafkaTemplate);
        exceptionPublisher.setTopic(Constants.EXCEPTION_TOPIC);
        return exceptionPublisher;
    }

    @Bean
    public KafkaDomainEventPublisher kafkaDomainEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        KafkaDomainEventPublisher domainEventPublisher = new KafkaDomainEventPublisher();
        domainEventPublisher.setProducer(kafkaTemplate);
        domainEventPublisher.setTopic(Constants.EVENT_TOPIC);
        return domainEventPublisher;
    }
}