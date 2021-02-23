package org.enodeframework.test.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.enodeframework.kafka.KafkaMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "kafka")
@Configuration
public class EnodeTestKafkaConfig {

    @Value("${spring.enode.mq.topic.command}")
    private String commandTopic;

    @Value("${spring.enode.mq.topic.event}")
    private String eventTopic;

    @Value("${spring.enode.mq.topic.application}")
    private String applicationTopic;

    @Value("${spring.enode.mq.topic.exception}")
    private String exceptionTopic;

    @Autowired
    @Qualifier("kafkaCommandListener")
    private KafkaMessageListener kafkaCommandListener;

    @Autowired
    @Qualifier("kafkaDomainEventListener")
    private KafkaMessageListener kafkaDomainEventListener;

    @Autowired
    @Qualifier("kafkaApplicationMessageListener")
    private KafkaMessageListener kafkaApplicationMessageListener;

    @Autowired
    @Qualifier("kafkaPublishableExceptionListener")
    private KafkaMessageListener kafkaPublishableExceptionListener;

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
    public ConcurrentMessageListenerContainer<String, String> commandListenerContainer(ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(commandTopic);
        properties.setGroupId(Constants.DEFAULT_CONSUMER_GROUP);
        properties.setMessageListener(kafkaCommandListener);
        properties.setMissingTopicsFatal(false);
        return new ConcurrentMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> domainEventListenerContainer(ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(eventTopic);
        properties.setGroupId(Constants.DEFAULT_PRODUCER_GROUP);
        properties.setMessageListener(kafkaDomainEventListener);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return new ConcurrentMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> applicationMessageListenerContainer(ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(applicationTopic);
        properties.setGroupId(Constants.DEFAULT_PRODUCER_GROUP);
        properties.setMessageListener(kafkaApplicationMessageListener);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return new ConcurrentMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, String> publishableExceptionListenerContainer(ConsumerFactory<String, String> consumerFactory) {
        ContainerProperties properties = new ContainerProperties(exceptionTopic);
        properties.setGroupId(Constants.DEFAULT_PRODUCER_GROUP);
        properties.setMessageListener(kafkaPublishableExceptionListener);
        properties.setMissingTopicsFatal(false);
        properties.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        return new ConcurrentMessageListenerContainer<>(consumerFactory, properties);
    }

    @Bean(name = "enodeKafkaTemplate")
    public KafkaTemplate<String, String> enodeKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
