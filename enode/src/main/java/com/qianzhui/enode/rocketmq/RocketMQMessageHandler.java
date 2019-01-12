package com.qianzhui.enode.rocketmq;

import com.alibaba.rocketmq.common.message.MessageExt;
import com.qianzhui.enode.common.rocketmq.consumer.listener.CompletableConsumeConcurrentlyContext;

public interface RocketMQMessageHandler {
    boolean isMatched(TopicTagData topicTagData);

    void handle(MessageExt message, CompletableConsumeConcurrentlyContext context);
}
