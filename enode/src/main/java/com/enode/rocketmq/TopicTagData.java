package com.enode.rocketmq;

public class TopicTagData {

    private String topic;
    private String tag;

    public TopicTagData() {

    }

    public TopicTagData(String topic, String tag) {
        this.topic = topic;
        this.tag = tag;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TopicTagData that = (TopicTagData) o;

        if (!topic.equals(that.topic)) {
            return false;
        }
        return tag.equals(that.tag);

    }

    @Override
    public int hashCode() {
        int result = topic.hashCode();
        result = 31 * result + tag.hashCode();
        return result;
    }
}
