package com.enode.samples.providers;

import com.enode.eventing.IDomainEvent;
import com.enode.rocketmq.AbstractTopicProvider;
import com.enode.rocketmq.TopicData;

import java.util.ArrayList;
import java.util.Collection;

public class EventTopicProvider extends AbstractTopicProvider<IDomainEvent> {
    @Override
    public TopicData getPublishTopic(IDomainEvent event) {
        return new TopicData("EnodeCommonTopicDev", "DomainEvent");
    }

    @Override
    public Collection<TopicData> getAllSubscribeTopics() {
        return new ArrayList<TopicData>() {{
            add(new TopicData("EnodeCommonTopicDev", "DomainEvent"));
        }};
    }
}
