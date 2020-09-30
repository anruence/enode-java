package com.microsoft.conference.registration.domain.seatassigning.Models;

import org.enodeframework.common.utilities.Ensure;

public class Attendee {
    public String firstName;
    public String lastName;
    public String email;

    public Attendee() {
    }

    public Attendee(String firstName, String lastName, String email) {
        Ensure.notNull(firstName, "firstName");
        Ensure.notNull(lastName, "lastName");
        Ensure.notNull(email, "email");
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
