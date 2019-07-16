package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;


@TableName(value = "conference_slug_index")
public class ConferenceSlugIndexDO {
    private String id;
    private String indexId;
    private String conferenceId;
    private String slug;

    public String getId() {
        return this.id;
    }

    public String getIndexId() {
        return this.indexId;
    }

    public String getConferenceId() {
        return this.conferenceId;
    }

    public String getSlug() {
        return this.slug;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIndexId(String indexId) {
        this.indexId = indexId;
    }

    public void setConferenceId(String conferenceId) {
        this.conferenceId = conferenceId;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
