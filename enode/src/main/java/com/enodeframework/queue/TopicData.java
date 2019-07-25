package com.enodeframework.queue;

/**
 * @author anruence@gmail.com
 */
public class TopicData {
    private String topic;
    private String tags;

    public TopicData(String topic, String tags) {
        this.topic = topic;
        this.tags = tags;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
