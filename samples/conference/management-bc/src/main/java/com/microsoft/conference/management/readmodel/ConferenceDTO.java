package com.microsoft.conference.management.readmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ConferenceDTO {
    private String id;
    private String name;
    private String description;
    private String location;
    private String tagline;
    private String twitterSearch;
    private Date startDate;
    private Date endDate;
    private Boolean isPublished;
    private String accessCode;
    private String ownerName;
    private String ownerEmail;
    private String slug;
    private boolean wasEverPublished;
}
