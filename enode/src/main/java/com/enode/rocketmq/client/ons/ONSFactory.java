package com.enode.rocketmq.client.ons;

import com.enode.rocketmq.client.Consumer;
import com.enode.rocketmq.client.Producer;
import com.enode.rocketmq.client.RocketMQFactory;

import java.util.Properties;

public class ONSFactory implements RocketMQFactory {
    @Override
    public Producer createProducer(Properties properties) {
        return new ONSProducerImpl(properties);
    }

    @Override
    public Consumer createPushConsumer(Properties properties) {
        return new ONSConsumerImpl(properties);
    }
}
