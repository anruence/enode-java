package com.microsoft.conference.management.domain.publishableexceptions;

import org.enodeframework.domain.DomainException;

import java.util.Map;

public class SeatInsufficientException extends DomainException {
    public String conferenceId;
    public String reservationId;

    public SeatInsufficientException(String conferenceId, String reservationId) {
        super();
        this.conferenceId = conferenceId;
        this.reservationId = reservationId;
    }

    @Override
    public void serializeTo(Map<String, Object> serializableInfo) {
        conferenceId = (String) serializableInfo.get("ConferenceId");
        reservationId = (String) serializableInfo.get("ReservationId");
    }

    @Override
    public void restoreFrom(Map<String, Object> serializableInfo) {
        serializableInfo.put("ConferenceId", conferenceId);
        serializableInfo.put("ReservationId", reservationId);
    }
}
