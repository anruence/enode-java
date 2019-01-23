package com.enode.queue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTopicProvider<T> implements ITopicProvider<T> {

    private Map<Class, TopicData> _topicDict = new HashMap<>();

    @Override
    public TopicData getPublishTopic(T source) {
        return _topicDict.get(source.getClass());
    }


    @Override
    public Collection<TopicData> getAllSubscribeTopics() {
        return _topicDict.values();
    }

    protected Collection<Class> getAllTypes() {
        return _topicDict.keySet();
    }

    protected void registerTopic(TopicData topic, Class[] types) {
        if (types == null || types.length == 0) {
            return;
        }

        for (Class type : types) {
            _topicDict.put(type, topic);
        }
    }
}
