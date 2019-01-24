package com.enode.samples.note.providers;

import com.enode.eventing.IDomainEvent;
import com.enode.queue.AbstractTopicProvider;
import com.enode.queue.TopicData;

import java.util.ArrayList;
import java.util.Collection;

public class EventTopicProvider extends AbstractTopicProvider<IDomainEvent> {
    @Override
    public TopicData getPublishTopic(IDomainEvent event) {
        return new TopicData("EnodeCommonTopicDev", "DevDomainEvent");
    }

    @Override
    public Collection<TopicData> getAllSubscribeTopics() {
        return new ArrayList<TopicData>() {{
            add(new TopicData("EnodeCommonTopic", "DevDomainEvent"));
        }};
    }
}
