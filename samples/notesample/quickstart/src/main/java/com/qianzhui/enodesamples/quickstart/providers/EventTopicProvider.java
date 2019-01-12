package com.qianzhui.enodesamples.quickstart.providers;

import com.qianzhui.enode.eventing.IDomainEvent;
import com.qianzhui.enode.rocketmq.AbstractTopicProvider;
import com.qianzhui.enode.rocketmq.TopicTagData;

import java.util.ArrayList;
import java.util.Collection;

public class EventTopicProvider extends AbstractTopicProvider<IDomainEvent> {
    @Override
    public TopicTagData getPublishTopic(IDomainEvent event) {
        return new TopicTagData("EnodeCommonTopicDev", "DomainEvent");
    }

    @Override
    public Collection<TopicTagData> getAllSubscribeTopics() {
        return new ArrayList<TopicTagData>() {{
            add(new TopicTagData("EnodeCommonTopicDev", "DomainEvent"));
        }};
    }
}
