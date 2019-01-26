package com.enode.samples.note.commandhandlers;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.enode.ENode;
import com.enode.commanding.ICommandService;
import com.enode.kafka.config.KafkaConfig;
import com.enode.rocketmq.client.impl.NativePropertyKey;
import com.enode.rocketmq.client.ons.PropertyKeyConst;
import com.enode.rocketmq.message.config.RocketMQConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class AppConfigCommandConsumer {

    //    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public KafkaConfig kafkaConfig() {

        /**============= Enode数据库配置（内存实现不需要配置） ===========*/
        Properties properties = new Properties();
        properties.setProperty("driverClassName", "com.mysql.jdbc.Driver");
        properties.setProperty("url", "jdbc:mysql://localhost:3306/enode");
        properties.setProperty("username", "root");
        properties.setProperty("password", "anruence");
        properties.setProperty("initialSize", "1");
        properties.setProperty("maxTotal", "1");
        /**=============================================================*/

        DataSource dataSource = null;
        try {
            dataSource = DruidDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ENode enode = ENode.create("com.enode.samples")
                .registerDefaultComponents();
//                .useMysqlComponents(dataSource); // 注销此行，启用内存实现（CommandStore,EventStore,SequenceMessagePublishedVersionStore,MessageHandleRecordStore）

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
        config.useKafka(producerProps, props, ENode.COMMAND_CONSUMER | ENode.PUBLISHERS, 6001);
        return config;
    }

    @Bean
    public ICommandService commandService() {
        return rocketMQConfig().getEnode().resolve(ICommandService.class);
    }


    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public RocketMQConfig rocketMQConfig() {
        /**============= Enode所需消息队列配置，RocketMQ实现 ======*/
        Properties nativeproducer = new Properties();
        nativeproducer.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        nativeproducer.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroup");

        Properties nativeconsumer = new Properties();
        nativeconsumer.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        nativeconsumer.setProperty(NativePropertyKey.ConsumerGroup, "NoteSampleConsumerGroupCommand");
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
//        config.useONS(onsproducer, onsconsumer, ENode.COMMAND_CONSUMER | ENode.PUBLISHERS, 6001);
        config.useNativeRocketMQ(nativeproducer, nativeconsumer, ENode.COMMAND_CONSUMER | ENode.PUBLISHERS, 6001);
        return config;
    }
}
