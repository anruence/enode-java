package com.microsoft.conference.management.domain.publishableexceptions;

import org.enodeframework.domain.DomainException;

import java.util.Map;

public class SeatInsufficientException extends DomainException {
    public String ConferenceId;
    public String ReservationId;

    public SeatInsufficientException(String conferenceId, String reservationId) {
        super();
        ConferenceId = conferenceId;
        ReservationId = reservationId;
    }

    @Override
    public void serializeTo(Map<String, Object> serializableInfo) {
        ConferenceId = (String) serializableInfo.get("ConferenceId");
        ReservationId = (String) serializableInfo.get("ReservationId");
    }

    @Override
    public void restoreFrom(Map<String, Object> serializableInfo) {
        serializableInfo.put("ConferenceId", ConferenceId);
        serializableInfo.put("ReservationId", ReservationId);
    }
}
