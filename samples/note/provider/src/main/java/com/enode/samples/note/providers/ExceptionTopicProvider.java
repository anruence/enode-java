package com.enode.samples.note.providers;

import com.enode.infrastructure.IPublishableException;
import com.enode.queue.AbstractTopicProvider;
import com.enode.queue.TopicTagData;

import java.util.ArrayList;
import java.util.Collection;

public class ExceptionTopicProvider extends AbstractTopicProvider<IPublishableException> {
    @Override
    public TopicTagData getPublishTopic(IPublishableException event) {
        return new TopicTagData("EnodeCommonTopicDev", "Exception");
    }

    @Override
    public Collection<TopicTagData> getAllSubscribeTopics() {
        return new ArrayList<TopicTagData>() {{
            add(new TopicTagData("EnodeCommonTopicDev", "Exception"));
        }};
    }
}
