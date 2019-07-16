package com.microsoft.conference.common.management.commands;

import org.enodeframework.commanding.Command;

import java.util.Date;

public class UpdateConference extends Command<String> {
    public String name;
    public String description;
    public String location;
    public String tagline;
    public String twitterSearch;
    public Date startDate;
    public Date endDate;
}
