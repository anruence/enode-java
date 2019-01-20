package com.enode.rocketmq.client;

import com.enode.rocketmq.TopicTagData;
import com.enode.rocketmq.client.consumer.listener.CompletableConsumeConcurrentlyContext;

public interface IMQMessageHandler {

    boolean isMatched(TopicTagData topicTagData);

    void handle(Object msg, CompletableConsumeConcurrentlyContext context);
}
