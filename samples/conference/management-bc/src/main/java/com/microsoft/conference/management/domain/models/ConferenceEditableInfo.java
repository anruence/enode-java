package com.microsoft.conference.management.domain.models;

import java.util.Date;

public class ConferenceEditableInfo {
    private String name;
    private String description;
    private String location;
    private String tagline;
    private String twitterSearch;
    private Date startDate;
    private Date endDate;

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

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLocation() {
        return this.location;
    }

    public String getTagline() {
        return this.tagline;
    }

    public String getTwitterSearch() {
        return this.twitterSearch;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public void setTwitterSearch(String twitterSearch) {
        this.twitterSearch = twitterSearch;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
