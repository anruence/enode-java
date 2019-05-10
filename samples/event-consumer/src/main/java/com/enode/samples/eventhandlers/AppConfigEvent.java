package com.enode.samples.eventhandlers;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.enode.ENodeBootstrap;
import com.enode.queue.TopicData;
import com.enode.queue.command.CommandResultProcessor;
import com.enode.rocketmq.message.RocketMQApplicationMessagePublisher;
import com.enode.rocketmq.message.RocketMQDomainEventConsumer;
import com.enode.rocketmq.message.RocketMQDomainEventPublisher;
import com.enode.rocketmq.message.RocketMQPublishableExceptionPublisher;
import com.enode.rocketmq.message.config.RocketMQConfig;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfigEvent {

//    public KafkaConfig kafkaConfigEvent() {
//        Properties props = new Properties();
//        props.put("bootstrap.servers", "localhost:9092");
//        props.put("group.id", "test");
//        props.put("enable.auto.commit", "true");
//        props.put("auto.commit.interval.ms", "100");
//        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//
//        Properties producerProps = new Properties();
//        producerProps.put("bootstrap.servers", "localhost:9092");
//        producerProps.put("enable.idempotence", "true");
//        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        return null;
//    }

    public static String EVENT_TOPIC = "EventSample";

    public static String NAMESRVADDR = "127.0.0.1:9876";

    public static String EVENT_CONSUMER_GROUP = "EventConsumerGroup";

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setPackages(Lists.newArrayList("com.enode.samples"));
        return bootstrap;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public RocketMQConfig rocketMQConfig() {
        RocketMQConfig rocketMQConfig = new RocketMQConfig();
        rocketMQConfig.setRegisterFlag(RocketMQConfig.DOMAIN_EVENT_CONSUMER);
        return rocketMQConfig;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public RocketMQDomainEventConsumer domainEventConsumer() {
        return new RocketMQDomainEventConsumer();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public CommandResultProcessor commandResultProcessor() {
        CommandResultProcessor processor = new CommandResultProcessor(6002);
        return processor;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer eventConsumer(RocketMQDomainEventConsumer domainEventConsumer) {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(EVENT_CONSUMER_GROUP);
        defaultMQPushConsumer.setNamesrvAddr(NAMESRVADDR);
        Map<String, String> topic = new HashMap<>();
        topic.put(EVENT_TOPIC, "*");
        defaultMQPushConsumer.setSubscription(topic);
        defaultMQPushConsumer.setMessageListener(domainEventConsumer);
        return defaultMQPushConsumer;
    }

    @Bean
    public RocketMQApplicationMessagePublisher rocketMQApplicationMessagePublisher(DefaultMQProducer eventProducer) {
        RocketMQApplicationMessagePublisher applicationMessagePublisher = new RocketMQApplicationMessagePublisher();
        applicationMessagePublisher.setProducer(eventProducer);
        applicationMessagePublisher.setTopicData(new TopicData(EVENT_TOPIC, "*"));
        return applicationMessagePublisher;
    }

    @Bean
    public RocketMQPublishableExceptionPublisher rocketMQPublishableExceptionPublisher(DefaultMQProducer eventProducer) {
        RocketMQPublishableExceptionPublisher exceptionPublisher = new RocketMQPublishableExceptionPublisher();
        exceptionPublisher.setProducer(eventProducer);
        exceptionPublisher.setTopicData(new TopicData(EVENT_TOPIC, "*"));
        return exceptionPublisher;
    }


    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer eventProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(NAMESRVADDR);
        producer.setProducerGroup("EVENT_PRODUCER_GROUP");
        return producer;
    }

    @Bean
    public RocketMQDomainEventPublisher rocketMQDomainEventPublisher(DefaultMQProducer eventProducer) {
        RocketMQDomainEventPublisher domainEventPublisher = new RocketMQDomainEventPublisher();
        domainEventPublisher.setProducer(eventProducer);
        domainEventPublisher.setTopicData(new TopicData(EVENT_TOPIC, "*"));
        return domainEventPublisher;
    }
}
