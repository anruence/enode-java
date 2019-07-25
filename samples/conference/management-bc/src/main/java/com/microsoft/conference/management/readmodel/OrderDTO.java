package com.microsoft.conference.management.readmodel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderDTO {
    public String OrderId;
    public String ConferenceId;
    public String AccessCode;
    public String RegistrantFirstName;
    public String RegistrantLastName;
    public String RegistrantEmail;
    public BigDecimal TotalAmount;
    public int Status;
    private List<AttendeeDTO> _attendees = new ArrayList<>();

    public List<AttendeeDTO> GetAttendees() {
        return _attendees;
    }

    public void SetAttendees(List<AttendeeDTO> attendees) {
        _attendees = attendees;
    }

    public String GetStatusText() {
        return String.valueOf(Status);
    }
}
