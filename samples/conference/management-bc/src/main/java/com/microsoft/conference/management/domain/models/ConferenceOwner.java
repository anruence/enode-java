package com.microsoft.conference.management.domain.models;

public class ConferenceOwner {
    public String Name;
    public String Email;

    public ConferenceOwner() {
    }

    public ConferenceOwner(String name, String email) {
        Name = name;
        Email = email;
    }
}
