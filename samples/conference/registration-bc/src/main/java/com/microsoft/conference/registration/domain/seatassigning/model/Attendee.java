package com.microsoft.conference.registration.domain.seatassigning.model;

import lombok.Getter;
import lombok.Setter;
import org.enodeframework.common.utilities.Ensure;

@Getter
@Setter
public class Attendee {
    private String firstName;
    private String lastName;
    private String email;

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
