package com.microsoft.conference.management.domain.models;

import java.util.Date;

public class ConferenceInfo {
    public String AccessCode;
    public ConferenceOwner Owner;
    public String Slug;
    public String Name;
    public String Description;
    public String Location;
    public String Tagline;
    public String TwitterSearch;
    public Date StartDate;
    public Date EndDate;

    public ConferenceInfo() {
    }

    public ConferenceInfo(String accessCode, ConferenceOwner owner, String slug, String name, String description, String location, String tagline, String twitterSearch, Date startDate, Date endDate) {
        AccessCode = accessCode;
        Owner = owner;
        Slug = slug;
        Name = name;
        Description = description;
        Location = location;
        Tagline = tagline;
        TwitterSearch = twitterSearch;
        StartDate = startDate;
        EndDate = endDate;
    }
}
