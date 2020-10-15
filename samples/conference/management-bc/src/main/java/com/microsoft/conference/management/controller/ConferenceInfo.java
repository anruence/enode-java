package com.microsoft.conference.management.controller;

import com.microsoft.conference.management.domain.model.SeatType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The full conference information.
 * This class inherits from <see href="EditableConferenceInfo"/>
 * and exposes more information that is not user-editable once
 * it has been generated or provided.
 **/
@Getter
@Setter
public class ConferenceInfo extends EditableConferenceInfo {

    public ConferenceInfo() {
        this.Id = "";
        this.Seats = new ArrayList<>();
        this.AccessCode = "xxxxxx";
    }

    private String Id;

    private String AccessCode;

    private String OwnerName;
    private String OwnerEmail;

    private String Slug;

    private Boolean WasEverPublished;

    private List<SeatType> Seats;
}
