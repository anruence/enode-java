package com.enode.queue;

public class TopicData {

    private String topic;
    private String tag;

    public TopicData(String topic, String tag) {
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
        TopicData that = (TopicData) o;

        if (topic == null || tag == null) {
            return false;
        }

        if (!topic.equals(that.topic)) {
            return false;
        }
        return tag.equals(that.tag);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
