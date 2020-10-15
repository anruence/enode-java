package com.microsoft.conference.registration.readmodel.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderSeatAssignment {
    private String assignmentsId;
    private int position;
    private String seatTypeName;
    private String attendeeEmail;
    private String attendeeFirstName;
    private String attendeeLastName;
}
