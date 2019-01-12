package com.qianzhui.enodesamples.quickstart.providers;

import com.qianzhui.enode.infrastructure.IPublishableException;
import com.qianzhui.enode.rocketmq.AbstractTopicProvider;
import com.qianzhui.enode.rocketmq.TopicTagData;

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
