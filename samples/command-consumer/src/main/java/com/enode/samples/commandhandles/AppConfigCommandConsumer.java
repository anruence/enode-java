package com.enode.samples.commandhandles;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.enode.ENodeBootstrap;
import com.enode.commanding.impl.DefaultCommandProcessor;
import com.enode.commanding.impl.DefaultProcessingCommandHandler;
import com.enode.eventing.impl.DefaultEventService;
import com.enode.queue.TopicData;
import com.enode.queue.command.CommandResultProcessor;
import com.enode.rocketmq.message.RocketMQApplicationMessagePublisher;
import com.enode.rocketmq.message.RocketMQCommandConsumer;
import com.enode.rocketmq.message.RocketMQDomainEventPublisher;
import com.enode.rocketmq.message.RocketMQPublishableExceptionPublisher;
import com.enode.rocketmq.message.config.RocketMQConfig;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class AppConfigCommandConsumer {


    public static String COMMAND_TOPIC = "CommandSample";

    public static String EVENT_TOPIC = "EventSample";

    public static String NAMESRVADDR = "127.0.0.1:9876";

    public static String EVENT_PRODUCER_GROUP = "EventProducerGroup";

    public static String COMMAND_CONSUMER_GROUP = "CommandConsumerGroup";

//    //    @Bean(initMethod = "start", destroyMethod = "shutdown")
//    public KafkaConfig kafkaConfig() {
//
//        /**============= Enode数据库配置（内存实现不需要配置） ===========*/
//        Properties properties = new Properties();
//        properties.setProperty("driverClassName", "com.mysql.jdbc.Driver");
//        properties.setProperty("url", "jdbc:mysql://localhost:3306/enode");
//        properties.setProperty("username", "root");
//        properties.setProperty("password", "anruence");
//        properties.setProperty("initialSize", "1");
//        properties.setProperty("maxTotal", "1");
//        /**=============================================================*/
//
//        DataSource dataSource = null;
//        try {
//            dataSource = DruidDataSourceFactory.createDataSource(properties);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
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
//        KafkaConfig config = new KafkaConfig();
//        return config;
//    }


    @Bean
    public DefaultProcessingCommandHandler defaultProcessingCommandHandler() {
        return new DefaultProcessingCommandHandler();
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public DefaultEventService defaultEventService() {
        return new DefaultEventService();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public RocketMQConfig rocketMQConfig() {
        RocketMQConfig rocketMQConfig = new RocketMQConfig();
        rocketMQConfig.setRegisterFlag(RocketMQConfig.COMMAND_CONSUMER
                | RocketMQConfig.DOMAIN_EVENT_PUBLISHER
                | RocketMQConfig.APPLICATION_MESSAGE_PUBLISHER
                | RocketMQConfig.EXCEPTION_PUBLISHER
        );
        return rocketMQConfig;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer defaultMQPushConsumer(RocketMQCommandConsumer commandConsumer) {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(COMMAND_CONSUMER_GROUP);
        defaultMQPushConsumer.setNamesrvAddr(NAMESRVADDR);
        Map<String, String> topic = new HashMap<>();
        topic.put(COMMAND_TOPIC, "*");
        defaultMQPushConsumer.setSubscription(topic);
        defaultMQPushConsumer.setMessageListener(commandConsumer);
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
    public DefaultCommandProcessor defaultCommandProcessor() {
        return new DefaultCommandProcessor();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public RocketMQCommandConsumer commandConsumer() {
        RocketMQCommandConsumer commandConsumer = new RocketMQCommandConsumer();
        return commandConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public CommandResultProcessor commandResultProcessor() {
        CommandResultProcessor processor = new CommandResultProcessor(6001);
        return processor;
    }

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setPackages(Lists.newArrayList("com.enode.samples"));
        return bootstrap;
    }

}
