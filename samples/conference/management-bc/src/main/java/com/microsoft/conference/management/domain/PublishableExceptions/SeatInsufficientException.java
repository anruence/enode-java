package com.microsoft.conference.management.domain.PublishableExceptions;

import com.enodeframework.infrastructure.PublishableException;

import java.util.Map;

public class SeatInsufficientException extends PublishableException {
    public String ConferenceId;
    public String ReservationId;

    public SeatInsufficientException(String conferenceId, String reservationId) {
        super();
        ConferenceId = conferenceId;
        ReservationId = reservationId;
    }

    @Override
    public void serializeTo(Map<String, String> serializableInfo) {
        ConferenceId = serializableInfo.get("ConferenceId");
        ReservationId = serializableInfo.get("ReservationId");
    }

    @Override
    public void restoreFrom(Map<String, String> serializableInfo) {
        serializableInfo.put("ConferenceId", ConferenceId);
        serializableInfo.put("ReservationId", ReservationId);
    }
}
