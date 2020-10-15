package com.microsoft.conference.registration.domain.order.model;

import lombok.Getter;
import lombok.Setter;
import org.enodeframework.common.utilities.Ensure;

@Getter
@Setter
public class Registrant {
    private String firstName;
    private String lastName;
    private String email;

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
