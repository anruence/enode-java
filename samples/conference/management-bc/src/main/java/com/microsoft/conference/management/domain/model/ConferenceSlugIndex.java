package com.microsoft.conference.management.domain.model;

public class ConferenceSlugIndex {
    private String indexId;
    private String conferenceId;
    private String slug;

    public ConferenceSlugIndex() {
    }

    public ConferenceSlugIndex(String indexId, String conferenceId, String slug) {
        this.indexId = indexId;
        this.conferenceId = conferenceId;
        this.slug = slug;
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
