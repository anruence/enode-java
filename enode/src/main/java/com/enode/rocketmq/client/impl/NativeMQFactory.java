package com.enode.rocketmq.client.impl;

import com.enode.rocketmq.client.Consumer;
import com.enode.rocketmq.client.Producer;
import com.enode.rocketmq.client.RocketMQFactory;

import java.util.Properties;

public class NativeMQFactory implements RocketMQFactory {
    @Override
    public Producer createProducer(Properties properties) {
        return new NativeMQProducer(properties);
    }

    @Override
    public Consumer createPushConsumer(Properties properties) {
        return new NativeMQConsumer(properties);
    }
}
