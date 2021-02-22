package org.enodeframework.test.config;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.rocketmq.message.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "spring.enode", name = "mq", havingValue = "rocketmq")
@Configuration
public class EnodeTestRocketMQConfig {

    @Value("${spring.enode.mq.topic.command}")
    private String commandTopic;

    @Value("${spring.enode.mq.topic.event}")
    private String eventTopic;

    @Value("${spring.enode.mq.topic.application}")
    private String applicationTopic;

    @Value("${spring.enode.mq.topic.exception}")
    private String exceptionTopic;

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer commandConsumer(RocketMQMessageListener commandListener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(Constants.DEFAULT_CONSUMER_GROUP0);
        defaultMQPushConsumer.setNamesrvAddr(Constants.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(commandTopic, "*");
        defaultMQPushConsumer.setMessageListener(commandListener);
        defaultMQPushConsumer.setConsumeMessageBatchMaxSize(200);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer domainEventConsumer(RocketMQMessageListener listener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(Constants.DEFAULT_CONSUMER_GROUP1);
        defaultMQPushConsumer.setNamesrvAddr(Constants.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(eventTopic, "*");
        defaultMQPushConsumer.setMessageListener(listener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer applicationConsumer(RocketMQMessageListener listener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(Constants.DEFAULT_CONSUMER_GROUP2);
        defaultMQPushConsumer.setNamesrvAddr(Constants.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(applicationTopic, "*");
        defaultMQPushConsumer.setMessageListener(listener);
        return defaultMQPushConsumer;
    }

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQPushConsumer exceptionConsumer(RocketMQMessageListener listener) throws MQClientException {
        DefaultMQPushConsumer defaultMQPushConsumer = new DefaultMQPushConsumer();
        defaultMQPushConsumer.setConsumerGroup(Constants.DEFAULT_CONSUMER_GROUP3);
        defaultMQPushConsumer.setNamesrvAddr(Constants.NAMESRVADDR);
        defaultMQPushConsumer.subscribe(exceptionTopic, "*");
        defaultMQPushConsumer.setMessageListener(listener);
        return defaultMQPushConsumer;
    }

    @Bean(name = "enodeMQProducer", initMethod = "start", destroyMethod = "shutdown")
    public DefaultMQProducer defaultMQProducer() {
        DefaultMQProducer producer = new DefaultMQProducer();
        producer.setNamesrvAddr(Constants.NAMESRVADDR);
        producer.setProducerGroup(Constants.DEFAULT_PRODUCER_GROUP0);
        return producer;
    }
}
