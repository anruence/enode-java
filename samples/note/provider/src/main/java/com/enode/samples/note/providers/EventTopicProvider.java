package com.enode.samples.note.providers;

import com.enode.eventing.IDomainEvent;
import com.enode.queue.AbstractTopicProvider;
import com.enode.queue.TopicTagData;

import java.util.ArrayList;
import java.util.Collection;

public class EventTopicProvider extends AbstractTopicProvider<IDomainEvent> {
    @Override
    public TopicTagData getPublishTopic(IDomainEvent event) {
        return new TopicTagData("EnodeCommonTopicDev", "DevDomainEvent");
    }

    @Override
    public Collection<TopicTagData> getAllSubscribeTopics() {
        return new ArrayList<TopicTagData>() {{
            add(new TopicTagData("EnodeCommonTopic", "DevDomainEvent"));
        }};
    }
}
