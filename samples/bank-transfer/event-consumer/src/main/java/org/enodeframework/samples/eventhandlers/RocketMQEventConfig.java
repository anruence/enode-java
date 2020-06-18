package org.enodeframework.samples.eventhandlers;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.rocketmq.message.RocketMQApplicationMessageListener;
import org.enodeframework.rocketmq.message.RocketMQDomainEventListener;
import org.enodeframework.rocketmq.message.RocketMQPublishableExceptionListener;
import org.enodeframework.samples.QueueProperties;
import org.springframework.context.annotation.Bean;

public class RocketMQEventConfig {


    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer eventConsumer(RocketMQDomainEventListener domainEventListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(QueueProperties.EVENT_TOPIC, "*");
        defaultMQPushConsumer.setMessageListener(domainEventListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer applicationConsumer(RocketMQApplicationMessageListener applicationMessageListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP1);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(QueueProperties.APPLICATION_TOPIC, "*");
        defaultMQPushConsumer.setMessageListener(applicationMessageListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer exceptionConsumer(RocketMQPublishableExceptionListener publishableExceptionListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(QueueProperties.DEFAULT_CONSUMER_GROUP2);
        defaultMQPushConsumer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(QueueProperties.EXCEPTION_TOPIC, "*");
        defaultMQPushConsumer.setMessageListener(publishableExceptionListener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(QueueProperties.NAMESRVADDR);
        producer.setProducerGroup(QueueProperties.DEFAULT_PRODUCER_GROUP);
        return producer;
    }
}
