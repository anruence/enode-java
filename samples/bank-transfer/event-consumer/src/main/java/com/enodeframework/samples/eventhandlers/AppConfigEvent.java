package com.enodeframework.samples.eventhandlers;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.enodeframework.ENodeBootstrap;
import com.enodeframework.mysql.MysqlEventStore;
import com.enodeframework.mysql.MysqlPublishedVersionStore;
import com.enodeframework.queue.TopicData;
import com.enodeframework.queue.command.CommandResultProcessor;
import com.enodeframework.rocketmq.message.RocketMQApplicationMessagePublisher;
import com.enodeframework.rocketmq.message.RocketMQCommandService;
import com.enodeframework.rocketmq.message.RocketMQDomainEventListener;
import com.enodeframework.rocketmq.message.RocketMQDomainEventPublisher;
import com.enodeframework.rocketmq.message.RocketMQPublishableExceptionPublisher;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.enodeframework.samples.Constant.COMMAND_TOPIC;
import static com.enodeframework.samples.Constant.EVENT_CONSUMER_GROUP;
import static com.enodeframework.samples.Constant.EVENT_PRODUCER_GROUP;
import static com.enodeframework.samples.Constant.EVENT_TOPIC;
import static com.enodeframework.samples.Constant.NAMESRVADDR;

@Configuration
public class AppConfigEvent {

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setPackages(Lists.newArrayList("com.enodeframework.samples"));
        return bootstrap;
    }

    @Bean
    public RocketMQCommandService rocketMQCommandService(DefaultMQProducer eventProducer) {
        RocketMQCommandService rocketMQCommandService = new RocketMQCommandService();
        rocketMQCommandService.setDefaultMQProducer(eventProducer);
        TopicData topicData = new TopicData(COMMAND_TOPIC, "*");
        rocketMQCommandService.setTopicData(topicData);
        return rocketMQCommandService;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public CommandResultProcessor commandResultProcessor() {
        return new CommandResultProcessor(6001);
    }

    @Bean
    public RocketMQDomainEventListener domainEventListener() {
        return new RocketMQDomainEventListener();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer eventConsumer(RocketMQDomainEventListener domainEventListener) {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(EVENT_CONSUMER_GROUP);
        defaultMQPushConsumer.setNamesrvAddr(NAMESRVADDR);
        Map<String, String> topic = new HashMap<>();
        topic.put(EVENT_TOPIC, "*");
        defaultMQPushConsumer.setSubscription(topic);
        defaultMQPushConsumer.setMessageListener(domainEventListener);
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
        producer.setProducerGroup(EVENT_PRODUCER_GROUP);
        return producer;
    }

    @Bean
    public RocketMQDomainEventPublisher rocketMQDomainEventPublisher(DefaultMQProducer eventProducer) {
        RocketMQDomainEventPublisher domainEventPublisher = new RocketMQDomainEventPublisher();
        domainEventPublisher.setProducer(eventProducer);
        domainEventPublisher.setTopicData(new TopicData(EVENT_TOPIC, "*"));
        return domainEventPublisher;
    }

    @Bean
    public MysqlEventStore mysqlEventStore(HikariDataSource dataSource) {
        MysqlEventStore mysqlEventStore = new MysqlEventStore(dataSource, null);
        return mysqlEventStore;
    }

    @Bean
    public MysqlPublishedVersionStore mysqlPublishedVersionStore(HikariDataSource dataSource) {
        return new MysqlPublishedVersionStore(dataSource, null);
    }
}
