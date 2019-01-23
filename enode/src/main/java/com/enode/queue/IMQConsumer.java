package com.enode.queue;

public interface IMQConsumer {
    void registerMessageHandler(IMQMessageHandler handler);

    void subscribe(String topic, String subExpression);

    void start();

    void shutdown();
}