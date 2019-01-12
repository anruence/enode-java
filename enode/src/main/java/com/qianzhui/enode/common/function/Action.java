package com.qianzhui.enode.common.function;

import com.alibaba.rocketmq.client.exception.MQBrokerException;
import com.alibaba.rocketmq.client.exception.MQClientException;
import com.alibaba.rocketmq.remoting.exception.RemotingException;

import java.io.IOException;

public interface Action {
    void apply() throws Exception;
}
