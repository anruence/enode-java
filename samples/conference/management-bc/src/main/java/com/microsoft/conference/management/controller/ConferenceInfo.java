package com.microsoft.conference.management.controller;

import com.microsoft.conference.management.domain.models.SeatType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/// <summary>
/// The full conference information.
/// </summary>
/// <remarks>
/// This class inherits from <see cref="EditableConferenceInfo"/>
/// and exposes more information that is not user-editable once
/// it has been generated or provided.
/// </remarks>
@Getter
@Setter
public class ConferenceInfo extends EditableConferenceInfo {
    public ConferenceInfo() {
        this.Id = "";
        this.Seats = new ArrayList<>();
        this.AccessCode = "xxxxxx";
    }

    public String Id;

    public String AccessCode;

    public String OwnerName;
    public String OwnerEmail;

    public String Slug;

    public Boolean WasEverPublished;

    public List<SeatType> Seats;
}
