package com.qianzhui.enode.rocketmq;

public interface RocketMQMessageHandler {

    boolean isMatched(TopicTagData topicTagData);

    void handle(Object msg, Object context);
}
