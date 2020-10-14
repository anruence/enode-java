package com.microsoft.conference.management.readmodel;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrderDTO {
    private String orderId;
    private String conferenceId;
    private String accessCode;
    private String registrantFirstName;
    private String registrantLastName;
    private String registrantEmail;
    private BigDecimal totalAmount;
    private int status;
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
