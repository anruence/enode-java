package com.enode.queue;

import java.util.Collection;

public interface ITopicProvider<T> {
    TopicTagData getPublishTopic(T source);

    Collection<TopicTagData> getAllSubscribeTopics();
}
