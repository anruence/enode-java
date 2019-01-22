package com.enode.rocketmq.client;

import com.alibaba.rocketmq.client.producer.MessageQueueSelector;
import com.alibaba.rocketmq.client.producer.SendCallback;
import com.alibaba.rocketmq.common.message.Message;

public interface Producer {
    /**
     * 启动服务
     */
    void start();

    /**
     * 关闭服务
     */
    void shutdown();


    void send(final Message msg, final MessageQueueSelector selector, final Object arg, final SendCallback sendCallback);
}
