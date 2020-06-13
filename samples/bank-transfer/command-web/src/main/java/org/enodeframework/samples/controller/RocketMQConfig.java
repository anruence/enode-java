package org.enodeframework.samples.controller;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.rocketmq.message.RocketMQCommandService;
import org.enodeframework.samples.QueueProperties;
import org.springframework.context.annotation.Bean;

public class RocketMQConfig {
    @Bean
    public RocketMQCommandService rocketMQCommandService(DefaultMQProducer producer) {
        RocketMQCommandService rocketMQCommandService = new RocketMQCommandService();
        rocketMQCommandService.setDefaultMQProducer(producer);
        rocketMQCommandService.setTopic(QueueProperties.COMMAND_TOPIC);
        return rocketMQCommandService;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer commandProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        producer.setProducerGroup(QueueProperties.DEFAULT_PRODUCER_GROUP);
        return producer;
    }
}
