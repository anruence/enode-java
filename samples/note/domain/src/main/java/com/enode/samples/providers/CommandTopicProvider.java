package com.enode.samples.providers;

import com.enode.commanding.ICommand;
import com.enode.queue.AbstractTopicProvider;
import com.enode.queue.TopicTagData;

import java.util.ArrayList;
import java.util.Collection;

public class CommandTopicProvider extends AbstractTopicProvider<ICommand> {
    @Override
    public TopicTagData getPublishTopic(ICommand command) {
        return new TopicTagData("EnodeCommonTopicDevCommand", "*");
    }

    @Override
    public Collection<TopicTagData> getAllSubscribeTopics() {
        return new ArrayList<TopicTagData>() {{
            add(new TopicTagData("EnodeCommonTopicDevCommand", "*"));
        }};
    }
}
