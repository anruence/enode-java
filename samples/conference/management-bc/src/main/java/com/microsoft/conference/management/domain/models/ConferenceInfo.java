package com.microsoft.conference.management.domain.models;

import java.util.Date;

public class ConferenceInfo {
    public String accessCode;
    public ConferenceOwner owner;
    public String slug;
    public String name;
    public String description;
    public String location;
    public String tagline;
    public String twitterSearch;
    public Date startDate;
    public Date endDate;

    public ConferenceInfo() {
    }

    public ConferenceInfo(String accessCode, ConferenceOwner owner, String slug, String name, String description, String location, String tagline, String twitterSearch, Date startDate, Date endDate) {
        this.accessCode = accessCode;
        this.owner = owner;
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.location = location;
        this.tagline = tagline;
        this.twitterSearch = twitterSearch;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
