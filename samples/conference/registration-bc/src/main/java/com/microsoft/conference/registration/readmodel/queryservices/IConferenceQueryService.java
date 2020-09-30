package com.microsoft.conference.registration.readmodel.queryservices;

import java.util.List;

public interface IConferenceQueryService {
    ConferenceDetails getConferenceDetails(String slug);

    ConferenceAlias getConferenceAlias(String slug);

    List<ConferenceAlias> getPublishedConferences();

    List<SeatType> getPublishedSeatTypes(String conferenceId);

    List<SeatTypeName> getSeatTypeNames(List<String> seatTypes);
}
