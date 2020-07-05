package com.microsoft.conference.registration.readmodel.queryservices;

import java.util.List;

public interface IConferenceQueryService {
    ConferenceDetails GetConferenceDetails(String slug);

    ConferenceAlias GetConferenceAlias(String slug);

    List<ConferenceAlias> GetPublishedConferences();

    List<SeatType> GetPublishedSeatTypes(String conferenceId);

    List<SeatTypeName> GetSeatTypeNames(List<String> seatTypes);
}
