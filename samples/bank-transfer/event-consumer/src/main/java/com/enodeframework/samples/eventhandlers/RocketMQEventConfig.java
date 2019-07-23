package com.enodeframework.samples.eventhandlers;

import com.enodeframework.queue.TopicData;
import com.enodeframework.rocketmq.message.RocketMQApplicationMessageListener;
import com.enodeframework.rocketmq.message.RocketMQApplicationMessagePublisher;
import com.enodeframework.rocketmq.message.RocketMQCommandService;
import com.enodeframework.rocketmq.message.RocketMQDomainEventListener;
import com.enodeframework.rocketmq.message.RocketMQDomainEventPublisher;
import com.enodeframework.rocketmq.message.RocketMQPublishableExceptionListener;
import com.enodeframework.rocketmq.message.RocketMQPublishableExceptionPublisher;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

import static com.enodeframework.samples.QueueProperties.APPLICATION_TOPIC;
import static com.enodeframework.samples.QueueProperties.COMMAND_TOPIC;
import static com.enodeframework.samples.QueueProperties.DEFAULT_CONSUMER_GROUP;
import static com.enodeframework.samples.QueueProperties.DEFAULT_CONSUMER_GROUP1;
import static com.enodeframework.samples.QueueProperties.DEFAULT_CONSUMER_GROUP2;
import static com.enodeframework.samples.QueueProperties.DEFAULT_PRODUCER_GROUP;
import static com.enodeframework.samples.QueueProperties.EVENT_TOPIC;
import static com.enodeframework.samples.QueueProperties.EXCEPTION_TOPIC;
import static com.enodeframework.samples.QueueProperties.NAMESRVADDR;

public class RocketMQEventConfig {

    @Bean
    public RocketMQCommandService rocketMQCommandService(DefaultMQProducer defaultMQProducer) {
        RocketMQCommandService rocketMQCommandService = new RocketMQCommandService();
        rocketMQCommandService.setDefaultMQProducer(defaultMQProducer);
        TopicData topicData = new TopicData(COMMAND_TOPIC, "*");
        rocketMQCommandService.setTopicData(topicData);
        return rocketMQCommandService;
    }

    @Bean
    public RocketMQPublishableExceptionListener publishableExceptionListener() {
        return new RocketMQPublishableExceptionListener();
    }

    @Bean
    public RocketMQApplicationMessageListener applicationMessageListener() {
        return new RocketMQApplicationMessageListener();
    }

    @Bean
    public RocketMQDomainEventListener domainEventListener() {
        return new RocketMQDomainEventListener();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer eventConsumer(RocketMQDomainEventListener domainEventListener) {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(DEFAULT_CONSUMER_GROUP);
        defaultMQPushConsumer.setNamesrvAddr(NAMESRVADDR);
        Map<String, String> topic = new HashMap<>();
        topic.put(EVENT_TOPIC, "*");
        defaultMQPushConsumer.setSubscription(topic);
        defaultMQPushConsumer.setMessageListener(domainEventListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer applicationConsumer(RocketMQApplicationMessageListener applicationMessageListener) {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(DEFAULT_CONSUMER_GROUP1);
        defaultMQPushConsumer.setNamesrvAddr(NAMESRVADDR);
        Map<String, String> topic = new HashMap<>();
        topic.put(APPLICATION_TOPIC, "*");
        defaultMQPushConsumer.setSubscription(topic);
        defaultMQPushConsumer.setMessageListener(applicationMessageListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer exceptionConsumer(RocketMQPublishableExceptionListener publishableExceptionListener) {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(DEFAULT_CONSUMER_GROUP2);
        defaultMQPushConsumer.setNamesrvAddr(NAMESRVADDR);
        Map<String, String> topic = new HashMap<>();
        topic.put(EXCEPTION_TOPIC, "*");
        defaultMQPushConsumer.setSubscription(topic);
        defaultMQPushConsumer.setMessageListener(publishableExceptionListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(NAMESRVADDR);
        producer.setProducerGroup(DEFAULT_PRODUCER_GROUP);
        return producer;
    }

    @Bean
    public RocketMQDomainEventPublisher rocketMQDomainEventPublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQDomainEventPublisher domainEventPublisher = new RocketMQDomainEventPublisher();
        domainEventPublisher.setProducer(defaultMQProducer);
        domainEventPublisher.setTopicData(new TopicData(EVENT_TOPIC, "*"));
        return domainEventPublisher;
    }


    @Bean
    public RocketMQApplicationMessagePublisher rocketMQApplicationMessagePublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQApplicationMessagePublisher applicationMessagePublisher = new RocketMQApplicationMessagePublisher();
        applicationMessagePublisher.setProducer(defaultMQProducer);
        applicationMessagePublisher.setTopicData(new TopicData(APPLICATION_TOPIC, "*"));
        return applicationMessagePublisher;
    }

    @Bean
    public RocketMQPublishableExceptionPublisher rocketMQPublishableExceptionPublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQPublishableExceptionPublisher exceptionPublisher = new RocketMQPublishableExceptionPublisher();
        exceptionPublisher.setProducer(defaultMQProducer);
        exceptionPublisher.setTopicData(new TopicData(EXCEPTION_TOPIC, "*"));
        return exceptionPublisher;
    }

}
