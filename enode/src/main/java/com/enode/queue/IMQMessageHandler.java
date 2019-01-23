package com.enode.queue;

public interface IMQMessageHandler {

    boolean isMatched(TopicData topicTagData);

    void handle(String msg, CompletableConsumeConcurrentlyContext context);
}
