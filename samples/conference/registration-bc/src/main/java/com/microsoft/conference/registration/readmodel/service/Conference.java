package com.microsoft.conference.registration.readmodel.service;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class Conference {
    private String id;
    private String code;
    private String name;
    private String description;
    private String location;
    private String tagline;
    private String twitterSearch;
    private Date startDate;
    private boolean isPublished;

    public Conference(String id, String code, String name, String description, String location, String tagline, String twitterSearch, Date startDate, List<SeatTypeVO> seats) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.description = description;
        this.location = location;
        this.tagline = tagline;
        this.twitterSearch = twitterSearch;
        this.startDate = startDate;
    }

    public Conference() {
    }
}
