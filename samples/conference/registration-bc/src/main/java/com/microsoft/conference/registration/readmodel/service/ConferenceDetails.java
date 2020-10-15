package com.microsoft.conference.registration.readmodel.service;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class ConferenceDetails {
    private String id;
    private String slug;
    private String name;
    private String description;
    private String location;
    private String tagline;
    private String twitterSearch;
    private Date startDate;
}
