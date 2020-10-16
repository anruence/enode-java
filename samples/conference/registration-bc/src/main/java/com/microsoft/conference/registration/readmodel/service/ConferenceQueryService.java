package com.microsoft.conference.registration.readmodel.service;

import java.util.List;

public interface ConferenceQueryService {
    ConferenceDetails getConferenceDetails(String slug);

    ConferenceAlias getConferenceAlias(String slug);

    List<ConferenceAlias> getPublishedConferences();

    List<SeatTypeVO> getPublishedSeatTypes(String conferenceId);

    List<SeatTypeName> getSeatTypeNames(List<String> seatTypes);
}
