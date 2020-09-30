package com.microsoft.conference.management.domain.models;

import java.util.Date;

public class ConferenceEditableInfo {
    public String name;
    public String description;
    public String location;
    public String tagline;
    public String twitterSearch;
    public Date startDate;
    public Date endDate;

    public ConferenceEditableInfo() {
    }

    public ConferenceEditableInfo(String name, String description, String location, String tagline, String twitterSearch, Date startDate, Date endDate) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.tagline = tagline;
        this.twitterSearch = twitterSearch;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
