package com.qianzhui.enode.common.rocketmq.consumer.listener;

import com.alibaba.rocketmq.client.consumer.listener.MessageListener;
import com.alibaba.rocketmq.common.message.MessageExt;

import java.util.List;

public interface CompletableMessageListenerConcurrently extends MessageListener {
    void consumeMessage(final List<MessageExt> msgs,
                                                                final CompletableConsumeConcurrentlyContext context);
}
