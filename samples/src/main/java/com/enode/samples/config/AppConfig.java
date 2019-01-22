package com.enode.samples.config;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.enode.ENode;
import com.enode.commanding.ICommandService;
import com.enode.rocketmq.client.impl.NativePropertyKey;
import com.enode.rocketmq.client.ons.PropertyKeyConst;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class AppConfig {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public ENode eNode() {

        boolean isons = false;
        /**============= Enode所需消息队列配置，RocketMQ实现 ======*/
        Properties producerSetting = new Properties();
        producerSetting.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        producerSetting.setProperty(NativePropertyKey.ProducerGroup, "NoteSampleProducerGroup");

        Properties consumerSetting = new Properties();
        consumerSetting.setProperty(NativePropertyKey.NAMESRV_ADDR, "127.0.0.1:9876");
        consumerSetting.setProperty(NativePropertyKey.ConsumerGroup, "NoteSampleConsumerGroup");
        /**=============================================================*/

        /**============= Enode所需消息队列配置，ONS实现 ======*/
//        producerSetting = new Properties();
        producerSetting.setProperty(PropertyKeyConst.ProducerId, "PID_EnodeCommon");
        producerSetting.setProperty(PropertyKeyConst.AccessKey, "G6aUujQD6m1Uyy68");
        producerSetting.setProperty(PropertyKeyConst.SecretKey, "TR6MUs6R8dK6GTOKudmaaY80K2dmxI");

//        consumerSetting = new Properties();
        consumerSetting.setProperty(PropertyKeyConst.ConsumerId, "CID_NoteSample");
        consumerSetting.setProperty(PropertyKeyConst.AccessKey, "G6aUujQD6m1Uyy68");
        consumerSetting.setProperty(PropertyKeyConst.SecretKey, "TR6MUs6R8dK6GTOKudmaaY80K2dmxI");
        /**=============================================================*/

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
        if (true) {
            enode.useKafka(producerProps, props, 6000, ENode.ALL_COMPONENTS);
        } else {
            if (isons) {
                enode.useONS(producerSetting, consumerSetting, 6000,
                        ENode.ALL_COMPONENTS
                );
            } else {
                enode.useNativeRocketMQ(producerSetting, consumerSetting, 6000, ENode.COMMAND_SERVICE
                        | ENode.DOMAIN_EVENT_PUBLISHER
                        | ENode.DOMAIN_EVENT_CONSUMER
                        | ENode.COMMAND_CONSUMER);
            }
        }
        return enode;
    }

    @Bean
    public ICommandService commandService() {
        return eNode().getContainer().resolve(ICommandService.class);
    }
}
