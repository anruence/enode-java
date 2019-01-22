package com.enode.samples.providers;

import com.enode.commanding.ICommand;
import com.enode.rocketmq.AbstractTopicProvider;
import com.enode.rocketmq.TopicData;

import java.util.ArrayList;
import java.util.Collection;

public class CommandTopicProvider extends AbstractTopicProvider<ICommand> {
    @Override
    public TopicData getPublishTopic(ICommand command) {
        return new TopicData("EnodeCommonTopicDevCommand", "");
    }

    @Override
    public Collection<TopicData> getAllSubscribeTopics() {
        return new ArrayList<TopicData>() {{
            add(new TopicData("EnodeCommonTopicDevCommand", ""));
        }};
    }
}
