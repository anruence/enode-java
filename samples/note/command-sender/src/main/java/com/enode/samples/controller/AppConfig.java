package com.enode.samples.controller;

import com.enode.ENode;
import com.enode.commanding.ICommandService;
import com.enode.kafka.config.KafkaConfig;
import com.enode.rocketmq.client.impl.NativePropertyKey;
import com.enode.rocketmq.client.ons.PropertyKeyConst;
import com.enode.rocketmq.message.config.RocketMQConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class AppConfig {

    //    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public KafkaConfig kafkaConfig() {

        ENode enode = ENode.create("com.enode.samples").registerDefaultComponents();

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "100");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        Properties producerProps = new Properties();
        producerProps.put("bootstrap.servers", "localhost:9092");
        producerProps.put("enable.idempotence", "true");
        producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaConfig config = new KafkaConfig(enode);
        config.useKafka(producerProps, props, ENode.PUBLISHERS, 6000);
        return config;
    }

    @Bean
    public ICommandService commandService() {
        return rocketMQConfig().getEnode().resolve(ICommandService.class);
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public RocketMQConfig rocketMQConfig() {
        /**============= Enode所需消息队列配置，RocketMQ实现 ======*/
        Properties producer = new Properties();
        producer.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        producer.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroup");

        Properties consumer = new Properties();
        consumer.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        consumer.setProperty(NativePropertyKey.ConsumerGroup, "NoteSampleConsumerGroupCommand");
        /**=============================================================*/

        /**============= Enode所需消息队列配置，ONS实现 ======*/
        Properties onsproducer = new Properties();
        onsproducer.setProperty(PropertyKeyConst.ProducerId, "PID_EnodeCommon");
        onsproducer.setProperty(PropertyKeyConst.AccessKey, "G6aUujQD6m1Uyy68");
        onsproducer.setProperty(PropertyKeyConst.SecretKey, "TR6MUs6R8dK6GTOKudmaaY80K2dmxI");

        Properties onsconsumer = new Properties();
        onsconsumer.setProperty(PropertyKeyConst.ConsumerId, "CID_NoteSample");
        onsconsumer.setProperty(PropertyKeyConst.AccessKey, "G6aUujQD6m1Uyy68");
        onsconsumer.setProperty(PropertyKeyConst.SecretKey, "TR6MUs6R8dK6GTOKudmaaY80K2dmxI");
        /**=============================================================*/

        ENode enode = ENode.create("com.enode.samples").registerDefaultComponents();
        RocketMQConfig config = new RocketMQConfig(enode);
//        config.useONS(onsproducer, onsconsumer, ENode.PUBLISHERS, 6000);
        config.useNativeRocketMQ(producer, consumer, ENode.PUBLISHERS, 6000);
        return config;
    }
}
