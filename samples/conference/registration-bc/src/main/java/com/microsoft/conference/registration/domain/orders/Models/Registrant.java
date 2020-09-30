package com.microsoft.conference.registration.domain.orders.Models;

import org.enodeframework.common.utilities.Ensure;

public class Registrant {
    public String firstName;
    public String lastName;
    public String email;

    public Registrant() {
    }

    public Registrant(String firstName, String lastName, String email) {
        Ensure.notNull(firstName, "firstName");
        Ensure.notNull(lastName, "lastName");
        Ensure.notNull(email, "email");
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
