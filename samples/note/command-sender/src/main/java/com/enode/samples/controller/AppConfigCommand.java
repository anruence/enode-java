package com.enode.samples.controller;

import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.enode.ENodeBootstrap;
import com.enode.queue.TopicData;
import com.enode.queue.command.CommandResultProcessor;
import com.enode.rocketmq.message.RocketMQCommandService;
import com.enode.rocketmq.message.config.RocketMQConfig;
import com.google.common.collect.Lists;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfigCommand {

    public static String COMMAND_TOPIC = "CommandSample";

    public static String NAMESRVADDR = "127.0.0.1:9876";
    public static String PRODUCER_GROUP = "CommandGroup";

//    //    @Bean(initMethod = "start", destroyMethod = "shutdown")
//    public KafkaConfig kafkaConfig() {
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
////        config.useKafka(producerProps, props, ENode.PUBLISHERS, 6000);
//        return config;
//    }

    @Bean
    public RocketMQConfig rocketMQConfig() {
        RocketMQConfig rocketMQConfig = new RocketMQConfig();
        rocketMQConfig.setRegisterFlag(RocketMQConfig.COMMAND_SERVICE);
        return rocketMQConfig;
    }

    @Bean
    public RocketMQCommandService rocketMQCommandService(DefaultMQProducer producer) {
        RocketMQCommandService rocketMQCommandService = new RocketMQCommandService();
        rocketMQCommandService.setDefaultMQProducer(producer);
        TopicData topicData = new TopicData(COMMAND_TOPIC, "*");
        rocketMQCommandService.setTopicData(topicData);
        return rocketMQCommandService;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public CommandResultProcessor commandResultProcessor() {
        CommandResultProcessor processor = new CommandResultProcessor(6000);
        return processor;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer commandProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(NAMESRVADDR);
        producer.setProducerGroup(PRODUCER_GROUP);
        return producer;
    }

    @Bean(initMethod = "init")
    public ENodeBootstrap eNodeBootstrap() {
        ENodeBootstrap bootstrap = new ENodeBootstrap();
        bootstrap.setPackages(Lists.newArrayList("com.enode.samples"));
        return bootstrap;
    }

}
