package com.microsoft.conference.management.readmodel;

public class AttendeeDTO {
    private int position;
    private String seatTypeName;
    private String attendeeFirstName;
    private String attendeeLastName;
    private String attendeeEmail;

    public int getPosition() {
        return this.position;
    }

    public String getSeatTypeName() {
        return this.seatTypeName;
    }

    public String getAttendeeFirstName() {
        return this.attendeeFirstName;
    }

    public String getAttendeeLastName() {
        return this.attendeeLastName;
    }

    public String getAttendeeEmail() {
        return this.attendeeEmail;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setSeatTypeName(String seatTypeName) {
        this.seatTypeName = seatTypeName;
    }

    public void setAttendeeFirstName(String attendeeFirstName) {
        this.attendeeFirstName = attendeeFirstName;
    }

    public void setAttendeeLastName(String attendeeLastName) {
        this.attendeeLastName = attendeeLastName;
    }

    public void setAttendeeEmail(String attendeeEmail) {
        this.attendeeEmail = attendeeEmail;
    }
}
