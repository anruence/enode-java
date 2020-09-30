package com.microsoft.conference.management.domain.models;

public class ConferenceSlugIndex {
    public String indexId;
    public String conferenceId;
    public String slug;

    public ConferenceSlugIndex(String indexId, String conferenceId, String slug) {
        this.indexId = indexId;
        this.conferenceId = conferenceId;
        this.slug = slug;
    }
}
