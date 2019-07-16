package com.microsoft.conference.management.controller;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/// <summary>
/// Editable information about a conference.
/// </summary>
@Getter
@Setter
public class EditableConferenceInfo {

    public String Name;

    public String Description;

    public String Location;

    public String Tagline;
    public String TwitterSearch;

    public Date StartDate;

    public Date EndDate;

    public Boolean IsPublished;
}
