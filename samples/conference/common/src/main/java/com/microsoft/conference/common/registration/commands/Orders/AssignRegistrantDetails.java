package com.microsoft.conference.common.registration.commands.Orders;

import org.enodeframework.commanding.Command;

public class AssignRegistrantDetails extends Command<String> {
    public String FirstName;
    public String LastName;
    public String Email;

    public AssignRegistrantDetails() {
    }

    public AssignRegistrantDetails(String orderId) {
        super(orderId);
    }
}
