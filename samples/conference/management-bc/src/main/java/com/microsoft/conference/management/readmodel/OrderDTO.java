package com.microsoft.conference.management.readmodel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderDTO {
    public String orderId;
    public String conferenceId;
    public String accessCode;
    public String registrantFirstName;
    public String registrantLastName;
    public String registrantEmail;
    public BigDecimal totalAmount;
    public int status;
    private List<AttendeeDTO> attendees = new ArrayList<>();

    public List<AttendeeDTO> getAttendees() {
        return attendees;
    }

    public void setAttendees(List<AttendeeDTO> attendees) {
        this.attendees = attendees;
    }

    public String getStatusText() {
        return String.valueOf(status);
    }
}
