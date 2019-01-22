package com.enode.rocketmq.client;

import com.alibaba.rocketmq.client.consumer.DefaultMQPushConsumer;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.client.exception.MQClientException;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstractConsumer {

    protected final AtomicBoolean started = new AtomicBoolean(false);
    private final DefaultMQPushConsumer defaultMQPushConsumer;

    protected AbstractConsumer(Properties properties, MQClientInitializer mqClientInitializer) {
        mqClientInitializer.init(properties);

        this.defaultMQPushConsumer = initConsumer(properties, mqClientInitializer);
    }

    abstract protected DefaultMQPushConsumer initConsumer(Properties properties, MQClientInitializer mqClientInitializer);

    public void start() {
        try {
            if (this.started.compareAndSet(false, true)) {
                this.defaultMQPushConsumer.start();
            }
        } catch (Exception e) {
            throw new RocketMQClientException(e.getMessage());
        }
    }

    public void shutdown() {
        if (this.started.compareAndSet(true, false)) {
            this.defaultMQPushConsumer.shutdown();
        }
    }

    public void registerMessageListener(MessageListenerConcurrently messageListener) {
        if (null == messageListener) {
            throw new RocketMQClientException("listener is null");
        }

        this.defaultMQPushConsumer.registerMessageListener(messageListener);
    }

    public void subscribe(String topic, String subExpression) {
        if (null == topic) {
            throw new RocketMQClientException("topic is null");
        }

        try {
            this.defaultMQPushConsumer.subscribe(topic, subExpression);
        } catch (MQClientException e) {
            throw new RocketMQClientException("defaultMQPushConsumer subscribe exception", e);
        }
    }

    public void unsubscribe(String topic) {
        if (null != topic) {
            this.defaultMQPushConsumer.unsubscribe(topic);
        }
    }
}
