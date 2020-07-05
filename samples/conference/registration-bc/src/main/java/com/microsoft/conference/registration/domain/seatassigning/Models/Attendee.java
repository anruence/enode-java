package com.microsoft.conference.registration.domain.seatassigning.Models;

import org.enodeframework.common.utilities.Ensure;

public class Attendee {
    public String FirstName;
    public String LastName;
    public String Email;

    public Attendee() {
    }

    public Attendee(String firstName, String lastName, String email) {
        Ensure.notNull(firstName, "firstName");
        Ensure.notNull(lastName, "lastName");
        Ensure.notNull(email, "email");
        FirstName = firstName;
        LastName = lastName;
        Email = email;
    }
//        public override List<object> GetAtomicValues()
//        {
//            yield return FirstName;
//            yield return LastName;
//            yield return Email;
//        }
}
