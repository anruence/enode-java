package com.microsoft.conference.management.domain.models;

public class ConferenceSlugIndex {
    public String IndexId;
    public String ConferenceId;
    public String Slug;

    public ConferenceSlugIndex(String indexId, String conferenceId, String slug) {
        IndexId = indexId;
        ConferenceId = conferenceId;
        Slug = slug;
    }
}
