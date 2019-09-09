package org.enodeframework.samples.controller;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.queue.TopicData;
import org.enodeframework.rocketmq.message.RocketMQCommandService;
import org.enodeframework.samples.QueueProperties;
import org.springframework.context.annotation.Bean;

public class RocketMQConfig {
    @Bean
    public RocketMQCommandService rocketMQCommandService(DefaultMQProducer producer) {
        RocketMQCommandService rocketMQCommandService = new RocketMQCommandService();
        rocketMQCommandService.setDefaultMQProducer(producer);
        TopicData topicData = new TopicData(QueueProperties.COMMAND_TOPIC, "*");
        rocketMQCommandService.setTopicData(topicData);
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
