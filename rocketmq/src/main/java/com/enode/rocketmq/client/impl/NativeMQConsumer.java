package com.enode.rocketmq.client.impl;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.enode.common.Constants;
import com.enode.rocketmq.client.AbstractConsumer;
import com.enode.rocketmq.client.Consumer;
import com.enode.rocketmq.client.MQClientInitializer;

import java.util.Properties;

public class NativeMQConsumer extends AbstractConsumer implements Consumer {

    protected NativeMQConsumer(Properties properties) {
        super(properties, new MQClientInitializer());
    }

    @Override
    protected DefaultMQPushConsumer initConsumer(Properties properties, MQClientInitializer mqClientInitializer) {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();

        String consumerGroup = properties.getProperty(NativePropertyKey.ConsumerGroup);
        if (null == consumerGroup) {
            consumerGroup = Constants.DEFAULT_ENODE_CONSUMER_GROUP;
        }

        consumer.setConsumerGroup(consumerGroup);
        consumer.setNamesrvAddr(mqClientInitializer.getNameServerAddr());
        consumer.setInstanceName(mqClientInitializer.buildIntanceName());

        return consumer;
    }
}
