package com.enode.samples.note.providers;

import com.enode.infrastructure.IApplicationMessage;
import com.enode.queue.AbstractTopicProvider;
import com.enode.queue.TopicData;

import java.util.ArrayList;
import java.util.Collection;

public class ApplicationTopicProvider extends AbstractTopicProvider<IApplicationMessage> {
    @Override
    public TopicData getPublishTopic(IApplicationMessage event) {
        return new TopicData("EnodeCommonTopicDev", "Application");
    }

    @Override
    public Collection<TopicData> getAllSubscribeTopics() {
        return new ArrayList<TopicData>() {{
            add(new TopicData("EnodeCommonTopicDev", "Application"));
        }};
    }
}
