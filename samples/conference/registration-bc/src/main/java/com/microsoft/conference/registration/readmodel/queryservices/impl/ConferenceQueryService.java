package com.microsoft.conference.registration.readmodel.queryservices.impl;

import com.microsoft.conference.registration.readmodel.queryservices.ConferenceAlias;
import com.microsoft.conference.registration.readmodel.queryservices.ConferenceDetails;
import com.microsoft.conference.registration.readmodel.queryservices.IConferenceQueryService;
import com.microsoft.conference.registration.readmodel.queryservices.SeatType;
import com.microsoft.conference.registration.readmodel.queryservices.SeatTypeName;

import java.util.List;

public class ConferenceQueryService implements IConferenceQueryService {
    @Override
    public ConferenceDetails getConferenceDetails(String slug) {
        return null;
    }

    @Override
    public ConferenceAlias getConferenceAlias(String slug) {
        return null;
    }

    @Override
    public List<ConferenceAlias> getPublishedConferences() {
        return null;
    }

    @Override
    public List<SeatType> getPublishedSeatTypes(String conferenceId) {
        return null;
    }

    @Override
    public List<SeatTypeName> getSeatTypeNames(List<String> seatTypes) {
        return null;
    }
//    public ConferenceDetails GetConferenceDetails(String slug) {
//        using(var connection = GetConnection())
//        {
//            return connection.QueryList < ConferenceDetails > (new {
//            Slug = slug
//        },ConfigSettings.ConferenceTable).SingleOrDefault();
//        }
//    }
//
//    public ConferenceAlias GetConferenceAlias(String slug) {
//        using(var connection = GetConnection())
//        {
//            return connection.QueryList < ConferenceAlias > (new {
//            Slug = slug
//        },ConfigSettings.ConferenceTable).SingleOrDefault();
//        }
//    }
//
//    public List<ConferenceAlias> GetPublishedConferences() {
//        using(var connection = GetConnection())
//        {
//            return connection.QueryList < ConferenceAlias > (new {
//            IsPublished = 1
//        },ConfigSettings.ConferenceTable).ToList();
//        }
//    }
//
//    public List<SeatType> GetPublishedSeatTypes(String conferenceId) {
//        using(var connection = GetConnection())
//        {
//            return connection.QueryList < SeatType > (new {
//            ConferenceId = conferenceId
//        },ConfigSettings.SeatTypeTable).ToList();
//        }
//    }
//
//    public List<SeatTypeName> GetSeatTypeNames(List<String> seatTypes) {
//        var distinctIds = seatTypes.Distinct().ToArray();
//        if (distinctIds.Length == 0) {
//            return new List<SeatTypeName>();
//        }
//
//        using(var connection = GetConnection())
//        {
//            var result = new List<SeatTypeName>();
//            for (var seatId in distinctIds)
//            {
//                var seat = connection.QueryList < SeatType > (new {
//                Id = seatId
//            },ConfigSettings.SeatTypeTable).SingleOrDefault();
//                if (seat != null) {
//                    result.add(new SeatTypeName {
//                        Id = seat.Id, Name = seat.Name
//                    });
//                }
//            }
//            return result;
//        }
//    }
//
//    private IDbConnection GetConnection() {
//        return new SqlConnection(ConfigSettings.ConferenceConnectionString);
//    }
}