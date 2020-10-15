package com.microsoft.conference.common.registration.commands.order;

import org.enodeframework.commanding.Command;

public class AssignRegistrantDetails extends Command<String> {
    public String firstName;
    public String lastName;
    public String email;

    public AssignRegistrantDetails() {
    }

    public AssignRegistrantDetails(String orderId) {
        super(orderId);
    }
}
