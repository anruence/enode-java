package com.enode.rocketmq.client;

import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.enode.rocketmq.client.consumer.listener.CompletableMessageListenerConcurrently;

public interface Consumer {
    void start();

    void shutdown();

    void registerMessageListener(final MessageListenerConcurrently messageListener);

    void subscribe(final String topic, final String subExpression);

    void unsubscribe(final String topic);
}
