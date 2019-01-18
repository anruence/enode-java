package com.enode.rocketmq.trace.core.common;

import com.alibaba.rocketmq.common.message.MessageType;
import com.enode.rocketmq.trace.core.utils.MixUtils;

public class OnsTraceBean {
    private static String LOCAL_ADDRESS = MixUtils.getLocalAddress();
    private String topic = "";
    private String msgId = "";
    private String offsetMsgId = "";
    private String tags = "";
    private String keys = "";
    private String storeHost;
    private String clientHost;
    private long storeTime;
    private int retryTimes;
    private int bodyLength;
    private MessageType msgType;

    public OnsTraceBean() {
        this.storeHost = LOCAL_ADDRESS;
        this.clientHost = LOCAL_ADDRESS;
    }

    public MessageType getMsgType() {
        return this.msgType;
    }

    public void setMsgType(MessageType msgType) {
        this.msgType = msgType;
    }

    public String getOffsetMsgId() {
        return this.offsetMsgId;
    }

    public void setOffsetMsgId(String offsetMsgId) {
        this.offsetMsgId = offsetMsgId;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMsgId() {
        return this.msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTags() {
        return this.tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getKeys() {
        return this.keys;
    }

    public void setKeys(String keys) {
        this.keys = keys;
    }

    public String getStoreHost() {
        return this.storeHost;
    }

    public void setStoreHost(String storeHost) {
        this.storeHost = storeHost;
    }

    public String getClientHost() {
        return this.clientHost;
    }

    public void setClientHost(String clientHost) {
        this.clientHost = clientHost;
    }

    public long getStoreTime() {
        return this.storeTime;
    }

    public void setStoreTime(long storeTime) {
        this.storeTime = storeTime;
    }

    public int getRetryTimes() {
        return this.retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public int getBodyLength() {
        return this.bodyLength;
    }

    public void setBodyLength(int bodyLength) {
        this.bodyLength = bodyLength;
    }
}
