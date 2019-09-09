package org.enodeframework.samples.commandhandles;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.queue.TopicData;
import org.enodeframework.rocketmq.message.RocketMQApplicationMessagePublisher;
import org.enodeframework.rocketmq.message.RocketMQCommandListener;
import org.enodeframework.rocketmq.message.RocketMQDomainEventPublisher;
import org.enodeframework.rocketmq.message.RocketMQPublishableExceptionPublisher;
import org.enodeframework.samples.QueueProperties;
import org.springframework.context.annotation.Bean;

public class RocketMQCommandConfig {
    @Bean
    public RocketMQCommandListener commandListener() {
        return new RocketMQCommandListener();
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer defaultMQPushConsumer(RocketMQCommandListener commandListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP3);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(QueueProperties.COMMAND_TOPIC, "*");
        defaultMQPushConsumer.setMessageListener(commandListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        producer.setProducerGroup(QueueProperties.DEFAULT_PRODUCER_GROUP);
        return producer;
    }

    @Bean
    public RocketMQDomainEventPublisher rocketMQDomainEventPublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQDomainEventPublisher domainEventPublisher = new RocketMQDomainEventPublisher();
        domainEventPublisher.setProducer(defaultMQProducer);
        domainEventPublisher.setTopicData(new TopicData(QueueProperties.EVENT_TOPIC, "*"));
        return domainEventPublisher;
    }

    /**
     * 应用消息生产者，复用生产者实例发送到不同topic中
     */
    @Bean
    public RocketMQApplicationMessagePublisher rocketMQApplicationMessagePublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQApplicationMessagePublisher applicationMessagePublisher = new RocketMQApplicationMessagePublisher();
        applicationMessagePublisher.setProducer(defaultMQProducer);
        applicationMessagePublisher.setTopicData(new TopicData(QueueProperties.APPLICATION_TOPIC, "*"));
        return applicationMessagePublisher;
    }

    /**
     * 异常消息生产者，复用生产者实例发送到不同topic中
     */
    @Bean
    public RocketMQPublishableExceptionPublisher rocketMQPublishableExceptionPublisher(DefaultMQProducer defaultMQProducer) {
        RocketMQPublishableExceptionPublisher exceptionPublisher = new RocketMQPublishableExceptionPublisher();
        exceptionPublisher.setProducer(defaultMQProducer);
        exceptionPublisher.setTopicData(new TopicData(QueueProperties.EXCEPTION_TOPIC, "*"));
        return exceptionPublisher;
    }
}
