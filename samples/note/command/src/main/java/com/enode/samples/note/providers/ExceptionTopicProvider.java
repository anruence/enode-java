package com.enode.samples.note.providers;

import com.enode.infrastructure.IPublishableException;
import com.enode.queue.AbstractTopicProvider;
import com.enode.queue.TopicData;

import java.util.ArrayList;
import java.util.Collection;

public class ExceptionTopicProvider extends AbstractTopicProvider<IPublishableException> {
    @Override
    public TopicData getPublishTopic(IPublishableException event) {
        return new TopicData("EnodeCommonTopicDevException", "");
    }

    @Override
    public Collection<TopicData> getAllSubscribeTopics() {
        return new ArrayList<TopicData>() {{
            add(new TopicData("EnodeCommonTopicDevException", ""));
        }};
    }
}
