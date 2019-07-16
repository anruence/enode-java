package com.microsoft.conference.common.management.commands;

import com.enodeframework.commanding.Command;

import java.util.Date;

public class CreateConference extends Command<String> {
    public String AccessCode;
    public String OwnerName;
    public String OwnerEmail;
    public String Slug;
    public String Name;
    public String Description;
    public String Location;
    public String Tagline;
    public String TwitterSearch;
    public Date StartDate;
    public Date EndDate;
}
