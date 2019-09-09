package com.microsoft.conference.common.management.commands;

import org.enodeframework.commanding.Command;

import java.util.Date;

public class UpdateConference extends Command<String> {
    public String Name;
    public String Description;
    public String Location;
    public String Tagline;
    public String TwitterSearch;
    public Date StartDate;
    public Date EndDate;
}
