package com.enode.rocketmq.client;

import com.enode.rocketmq.TopicData;
import com.enode.rocketmq.client.consumer.listener.CompletableConsumeConcurrentlyContext;

public interface IMQMessageHandler {

    boolean isMatched(TopicData topicTagData);

    void handle(String msg, CompletableConsumeConcurrentlyContext context);
}
