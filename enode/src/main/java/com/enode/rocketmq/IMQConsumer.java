package com.enode.rocketmq;

import com.enode.rocketmq.client.IMQMessageHandler;

public interface IMQConsumer {
    void registerMessageHandler(IMQMessageHandler handler);

    void subscribe(String topic, String subExpression);

    void start();

    void shutdown();
}