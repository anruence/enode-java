package com.enode.rocketmq.client;

import com.enode.rocketmq.CompletableConsumeConcurrentlyContext;
import com.enode.rocketmq.TopicData;

public interface IMQMessageHandler {

    boolean isMatched(TopicData topicTagData);

    void handle(String msg, CompletableConsumeConcurrentlyContext context);
}
