package org.enodeframework.queue;

import java.io.Serializable;

/**
 * @author anruence@gmail.com
 */
public class QueueMessage implements Serializable {

    private String body;
    private String topic;
    /**
     * 业务标识
     */
    private String tag;
    private String routeKey;

    /**
     * 消息唯一标识
     */
    private String key;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
