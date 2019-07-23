package com.enodeframework.samples.commandhandles;

import com.enodeframework.queue.TopicData;
import com.enodeframework.rocketmq.message.RocketMQApplicationMessagePublisher;
import com.enodeframework.rocketmq.message.RocketMQCommandListener;
import com.enodeframework.rocketmq.message.RocketMQDomainEventPublisher;
import com.enodeframework.rocketmq.message.RocketMQPublishableExceptionPublisher;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

import static com.enodeframework.samples.QueueProperties.APPLICATION_TOPIC;
import static com.enodeframework.samples.QueueProperties.COMMAND_TOPIC;
import static com.enodeframework.samples.QueueProperties.DEFAULT_CONSUMER_GROUP3;
import static com.enodeframework.samples.QueueProperties.DEFAULT_PRODUCER_GROUP;
import static com.enodeframework.samples.QueueProperties.EVENT_TOPIC;
import static com.enodeframework.samples.QueueProperties.EXCEPTION_TOPIC;
import static com.enodeframework.samples.QueueProperties.NAMESRVADDR;

public class RocketMQCommandConfig {
    @Bean
    public RocketMQCommandListener commandListener() {
        return new RocketMQCommandListener();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer defaultMQPushConsumer(RocketMQCommandListener commandListener) {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(DEFAULT_CONSUMER_GROUP3);
        defaultMQPushConsumer.setNamesrvAddr(NAMESRVADDR);
        Map<String, String> topic = new HashMap<>();
        topic.put(COMMAND_TOPIC, "*");
        defaultMQPushConsumer.setSubscription(topic);
        defaultMQPushConsumer.setMessageListener(commandListener);
        return defaultMQPushConsumer;
    }


    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(NAMESRVADDR);
        producer.setProducerGroup(DEFAULT_PRODUCER_GROUP);
        return producer;
    }

    @Bean
    public RocketMQDomainEventPublisher rocketMQDomainEventPublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQDomainEventPublisher domainEventPublisher = new RocketMQDomainEventPublisher();
        domainEventPublisher.setProducer(defaultMQProducer);
        domainEventPublisher.setTopicData(new TopicData(EVENT_TOPIC, "*"));
        return domainEventPublisher;
    }

    /**
     * 应用消息生产者，复用生产者实例发送到不同topic中
     */
    @Bean
    public RocketMQApplicationMessagePublisher rocketMQApplicationMessagePublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQApplicationMessagePublisher applicationMessagePublisher = new RocketMQApplicationMessagePublisher();
        applicationMessagePublisher.setProducer(defaultMQProducer);
        applicationMessagePublisher.setTopicData(new TopicData(APPLICATION_TOPIC, "*"));
        return applicationMessagePublisher;
    }

    /**
     * 异常消息生产者，复用生产者实例发送到不同topic中
     */
    @Bean
    public RocketMQPublishableExceptionPublisher rocketMQPublishableExceptionPublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQPublishableExceptionPublisher exceptionPublisher = new RocketMQPublishableExceptionPublisher();
        exceptionPublisher.setProducer(defaultMQProducer);
        exceptionPublisher.setTopicData(new TopicData(EXCEPTION_TOPIC, "*"));
        return exceptionPublisher;
    }

}
