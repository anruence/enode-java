package com.enode.samples.providers;

import com.enode.infrastructure.IApplicationMessage;
import com.enode.queue.AbstractTopicProvider;
import com.enode.queue.TopicTagData;

import java.util.ArrayList;
import java.util.Collection;

public class ApplicationTopicProvider extends AbstractTopicProvider<IApplicationMessage> {
    @Override
    public TopicTagData getPublishTopic(IApplicationMessage event) {
        return new TopicTagData("EnodeCommonTopicDevApplication", "*");
    }

    @Override
    public Collection<TopicTagData> getAllSubscribeTopics() {
        return new ArrayList<TopicTagData>() {{
            add(new TopicTagData("EnodeCommonTopicDevApplication", "*"));
        }};
    }
}
