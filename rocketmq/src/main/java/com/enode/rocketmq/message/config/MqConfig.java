package com.enode.rocketmq.message.config;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.producer.DefaultMQProducer;
import com.enode.queue.TopicData;
import com.enode.rocketmq.message.RocketMQCommandConsumer;
import com.enode.rocketmq.message.RocketMQCommandService;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

public class MqConfig {

    @Bean
    RocketMQCommandService rocketMQCommandService(DefaultMQProducer producer) {
        RocketMQCommandService commandService = new RocketMQCommandService();
        commandService.setDefaultMQProducer(producer);
        TopicData topicData = new TopicData("CommandTopic", "*");
        commandService.setTopicData(topicData);
        return commandService;
    }

    @Bean
    public DefaultMQPushConsumer commandConsumer(RocketMQCommandConsumer commandConsumer) {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup("");
        defaultMQPushConsumer.setNamesrvAddr("");
        Map<String, String> topic = new HashMap<>();
        topic.put("CommandTopic", "*");
        defaultMQPushConsumer.setSubscription(topic);
        defaultMQPushConsumer.setMessageListener(commandConsumer);
        return defaultMQPushConsumer;
    }

    @Bean
    public DefaultMQProducer commandProducer() {
        DefaultMQProducer defaultMQProducer = new DefaultMQProducer();
        defaultMQProducer.setProducerGroup("");
        defaultMQProducer.setNamesrvAddr("");
        return defaultMQProducer;
    }
}
