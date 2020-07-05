package com.microsoft.conference.registration.domain.orders.Models;

import org.enodeframework.common.utilities.Ensure;

public class Registrant {
    public String FirstName;
    public String LastName;
    public String Email;

    public Registrant() {
    }

    public Registrant(String firstName, String lastName, String email) {
        Ensure.notNull(firstName, "firstName");
        Ensure.notNull(lastName, "lastName");
        Ensure.notNull(email, "email");
        FirstName = firstName;
        LastName = lastName;
        Email = email;
    }
}
