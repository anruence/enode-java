package org.enodeframework.samples.eventhandlers;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.rocketmq.message.RocketMQApplicationMessageListener;
import org.enodeframework.rocketmq.message.RocketMQApplicationMessagePublisher;
import org.enodeframework.rocketmq.message.RocketMQCommandService;
import org.enodeframework.rocketmq.message.RocketMQDomainEventListener;
import org.enodeframework.rocketmq.message.RocketMQDomainEventPublisher;
import org.enodeframework.rocketmq.message.RocketMQPublishableExceptionListener;
import org.enodeframework.rocketmq.message.RocketMQPublishableExceptionPublisher;
import org.enodeframework.samples.QueueProperties;
import org.springframework.context.annotation.Bean;

public class RocketMQEventConfig {
    @Bean
    public RocketMQCommandService rocketMQCommandService(DefaultMQProducer defaultMQProducer) {
        RocketMQCommandService rocketMQCommandService = new RocketMQCommandService();
        rocketMQCommandService.setDefaultMQProducer(defaultMQProducer);
        rocketMQCommandService.setTopic(QueueProperties.COMMAND_TOPIC);
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
    public DefaultMQPushConsumer eventConsumer(RocketMQDomainEventListener domainEventListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(QueueProperties.EVENT_TOPIC, "*");
        defaultMQPushConsumer.setMessageListener(domainEventListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer applicationConsumer(RocketMQApplicationMessageListener applicationMessageListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP1);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(QueueProperties.APPLICATION_TOPIC,"*");
        defaultMQPushConsumer.setMessageListener(applicationMessageListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer exceptionConsumer(RocketMQPublishableExceptionListener publishableExceptionListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP2);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(QueueProperties.EXCEPTION_TOPIC, "*");
        defaultMQPushConsumer.setMessageListener(publishableExceptionListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        producer.setProducerGroup(QueueProperties.DEFAULT_PRODUCER_GROUP);
        return producer;
    }

    @Bean
    public RocketMQDomainEventPublisher rocketMQDomainEventPublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQDomainEventPublisher domainEventPublisher = new RocketMQDomainEventPublisher();
        domainEventPublisher.setProducer(defaultMQProducer);
        domainEventPublisher.setTopic(QueueProperties.EVENT_TOPIC);
        return domainEventPublisher;
    }

    @Bean
    public RocketMQApplicationMessagePublisher rocketMQApplicationMessagePublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQApplicationMessagePublisher applicationMessagePublisher = new RocketMQApplicationMessagePublisher();
        applicationMessagePublisher.setProducer(defaultMQProducer);
        applicationMessagePublisher.setTopic(QueueProperties.APPLICATION_TOPIC);
        return applicationMessagePublisher;
    }

    @Bean
    public RocketMQPublishableExceptionPublisher rocketMQPublishableExceptionPublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQPublishableExceptionPublisher exceptionPublisher = new RocketMQPublishableExceptionPublisher();
        exceptionPublisher.setProducer(defaultMQProducer);
        exceptionPublisher.setTopic(QueueProperties.EXCEPTION_TOPIC);
        return exceptionPublisher;
    }
}
