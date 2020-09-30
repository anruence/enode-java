package com.microsoft.conference.management.domain.models;

public class ConferenceOwner {
    public String name;
    public String email;

    public ConferenceOwner() {
    }

    public ConferenceOwner(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
