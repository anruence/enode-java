package com.enode.rocketmq.client;

import java.util.Properties;

public interface RocketMQFactory {
    Producer createProducer(final Properties properties);

    Consumer createPushConsumer(final Properties properties);
}
