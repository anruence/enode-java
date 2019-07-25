package com.microsoft.conference.management.readmodel;

import java.util.List;

public class ConferenceQueryService {
    public ConferenceDTO FindConference(String slug) {
//        using(var connection = GetConnection())
//        {
//            return connection.QueryList < ConferenceDTO > (new {
//            Slug = slug
//        },ConfigSettings.ConferenceTable).SingleOrDefault();
//        }
        return null;
    }

    public ConferenceDTO FindConference(String email, String accessCode) {
//        using(var connection = GetConnection())
//        {
//            return connection.QueryList < ConferenceDTO > (new {
//            OwnerEmail = email, AccessCode = accessCode
//        },ConfigSettings.ConferenceTable).SingleOrDefault();
//        }
        return null;
    }

    public List<SeatTypeDTO> FindSeatTypes(String conferenceId) {
//        using(var connection = GetConnection())
//        {
//            return connection.QueryList < SeatTypeDTO > (new {
//            ConferenceId = conferenceId
//        },ConfigSettings.SeatTypeTable).ToList();
//        }
        return null;
    }

    public SeatTypeDTO FindSeatType(String seatTypeId) {
//        using(var connection = GetConnection())
//        {
//            return connection.QueryList < SeatTypeDTO > (new {
//            Id = seatTypeId
//        },ConfigSettings.SeatTypeTable).SingleOrDefault();
//        }
        return null;
    }

    public List<OrderDTO> FindOrders(String conferenceId) {
//        using(var connection = GetConnection())
//        {
//            var orders = connection.QueryList < OrderDTO > (new {
//            ConferenceId = conferenceId
//        },ConfigSettings.OrderTable);
//            for (var order in orders)
//            {
//                order.SetAttendees(connection.QueryList < AttendeeDTO > (new {
//                OrderId = order.OrderId
//            },ConfigSettings.OrderSeatAssignmentsTable).ToList());
//            }
//            return orders;
//        }
        return null;
    }
//    private IDbConnection GetConnection() {
//        return new SqlConnection(ConfigSettings.ConferenceConnectionString);
//    }
}
