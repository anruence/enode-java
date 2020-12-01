package com.microsoft.conference.management.domain.model;

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

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return this.location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTagline() {
        return this.tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public String getTwitterSearch() {
        return this.twitterSearch;
    }

    public void setTwitterSearch(String twitterSearch) {
        this.twitterSearch = twitterSearch;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
