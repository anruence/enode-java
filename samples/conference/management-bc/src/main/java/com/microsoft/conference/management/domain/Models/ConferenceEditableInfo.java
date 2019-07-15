package com.microsoft.conference.management.domain.Models;

import java.util.Date;

public class ConferenceEditableInfo {
    public String Name;
    public String Description;
    public String Location;
    public String Tagline;
    public String TwitterSearch;
    public Date StartDate;
    public Date EndDate;

    public ConferenceEditableInfo() {
    }

    public ConferenceEditableInfo(String name, String description, String location, String tagline, String twitterSearch, Date startDate, Date endDate) {
        Name = name;
        Description = description;
        Location = location;
        Tagline = tagline;
        TwitterSearch = twitterSearch;
        StartDate = startDate;
        EndDate = endDate;
    }
}
