package org.enodeframework.samples.commandhandles;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.enodeframework.rocketmq.message.RocketMQCommandListener;
import org.enodeframework.samples.QueueProperties;
import org.springframework.context.annotation.Bean;

public class RocketMQCommandConfig {

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
}
