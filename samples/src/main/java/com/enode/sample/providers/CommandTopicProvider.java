package com.enode.sample.providers;

import com.enode.commanding.ICommand;
import com.enode.rocketmq.AbstractTopicProvider;
import com.enode.rocketmq.TopicTagData;

import java.util.ArrayList;
import java.util.Collection;

public class CommandTopicProvider extends AbstractTopicProvider<ICommand> {
    @Override
    public TopicTagData getPublishTopic(ICommand command) {
        return new TopicTagData("EnodeCommonTopicDev", "Command");
    }

    @Override
    public Collection<TopicTagData> getAllSubscribeTopics() {
        return new ArrayList<TopicTagData>() {{
            add(new TopicTagData("EnodeCommonTopicDev", "Command"));
        }};
    }
}
