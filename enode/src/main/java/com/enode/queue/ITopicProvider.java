package com.enode.queue;

import java.util.Collection;

public interface ITopicProvider<T> {
    TopicData getPublishTopic(T source);

    Collection<TopicData> getAllSubscribeTopics();
}
