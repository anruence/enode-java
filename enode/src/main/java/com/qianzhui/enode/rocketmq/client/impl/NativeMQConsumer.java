package com.qianzhui.enode.rocketmq.client.impl;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.qianzhui.enode.common.rocketmq.consumer.CompletableDefaultMQPushConsumer;
import com.qianzhui.enode.rocketmq.Constants;
import com.qianzhui.enode.rocketmq.client.AbstractConsumer;
import com.qianzhui.enode.rocketmq.client.Consumer;
import com.qianzhui.enode.rocketmq.client.MQClientInitializer;

import java.util.Properties;

public class NativeMQConsumer extends AbstractConsumer implements Consumer {

    protected NativeMQConsumer(Properties properties) {
        super(properties, new MQClientInitializer());
    }

    @Override
    protected CompletableDefaultMQPushConsumer initConsumer(Properties properties, MQClientInitializer mqClientInitializer) {
//        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer();
        CompletableDefaultMQPushConsumer consumer = new CompletableDefaultMQPushConsumer();

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
