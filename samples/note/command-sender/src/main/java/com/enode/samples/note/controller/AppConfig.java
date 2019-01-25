package com.enode.samples.note.controller;

import com.enode.ENode;
import com.enode.commanding.ICommandService;
import com.enode.kafka.config.KafkaConfig;
import com.enode.queue.QueueMessageTypeCode;
import com.enode.rocketmq.client.impl.NativePropertyKey;
import com.enode.rocketmq.client.ons.PropertyKeyConst;
import com.enode.rocketmq.message.config.RocketMQConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
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
        Properties producerSetting1 = new Properties();
        producerSetting1.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        producerSetting1.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroup");

        Properties producerSetting2 = new Properties();
        producerSetting2.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        producerSetting2.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroupEvent");

        Properties producerSetting3 = new Properties();
        producerSetting3.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        producerSetting3.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroupDevException");

        Properties producerSetting4 = new Properties();
        producerSetting4.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        producerSetting4.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroupDevApp");
        Map<Integer, Properties> propertiesMap = new HashMap<>();
        propertiesMap.put(QueueMessageTypeCode.CommandMessage.getValue(), producerSetting1);
        propertiesMap.put(QueueMessageTypeCode.DomainEventStreamMessage.getValue(), producerSetting2);
        propertiesMap.put(QueueMessageTypeCode.ExceptionMessage.getValue(), producerSetting3);
        propertiesMap.put(QueueMessageTypeCode.ApplicationMessage.getValue(), producerSetting4);


        Properties consumerSetting1 = new Properties();
        consumerSetting1.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        consumerSetting1.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroup");

        Properties consumerSetting2 = new Properties();
        consumerSetting2.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        consumerSetting2.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroupEvent");

        Properties consumerSetting3 = new Properties();
        consumerSetting3.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        consumerSetting3.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroupDevException");

        Properties consumerSetting4 = new Properties();
        consumerSetting4.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        consumerSetting4.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroupDevApp");
        Map<Integer, Properties> consumerMap = new HashMap<>();
        consumerMap.put(QueueMessageTypeCode.CommandMessage.getValue(), consumerSetting1);
        consumerMap.put(QueueMessageTypeCode.DomainEventStreamMessage.getValue(), consumerSetting2);
        consumerMap.put(QueueMessageTypeCode.ExceptionMessage.getValue(), consumerSetting3);
        consumerMap.put(QueueMessageTypeCode.ApplicationMessage.getValue(), consumerSetting4);

        Properties consumerSetting = new Properties();
        consumerSetting.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        consumerSetting.setProperty(NativePropertyKey.ConsumerGroup, "NoteSampleConsumerGroupDev");
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
        config.useNativeRocketMQ(producerSetting1, consumerSetting1, ENode.COMMAND_SERVICE | ENode.DOMAIN_EVENT_PUBLISHER, 6000);
        return config;
    }
}
