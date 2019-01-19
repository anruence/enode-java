package com.enode.rocketmq.client;

import com.alibaba.rocketmq.common.message.MessageExt;
import com.enode.rocketmq.TopicTagData;
import com.enode.rocketmq.consumer.listener.CompletableConsumeConcurrentlyContext;

public interface IMQMessageHandler {

    boolean isMatched(TopicTagData topicTagData);

    void handle(MessageExt msg, CompletableConsumeConcurrentlyContext context);
}
