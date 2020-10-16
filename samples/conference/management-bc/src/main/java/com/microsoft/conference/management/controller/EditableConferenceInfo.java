package com.microsoft.conference.management.controller;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

// Editable information about a conference.
@Getter
@Setter
public class EditableConferenceInfo {

    private String name;

    private String description;

    private String location;

    private String tagline;

    private String twitterSearch;

    private Date startDate;

    private Date endDate;

    private Boolean isPublished;
}
