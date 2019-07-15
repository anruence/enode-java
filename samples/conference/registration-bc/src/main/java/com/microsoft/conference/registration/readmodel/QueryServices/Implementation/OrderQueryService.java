package com.microsoft.conference.registration.readmodel.QueryServices.Implementation;

import com.microsoft.conference.registration.readmodel.QueryServices.IOrderQueryService;
import com.microsoft.conference.registration.readmodel.QueryServices.Order;
import com.microsoft.conference.registration.readmodel.QueryServices.OrderSeatAssignment;

public class OrderQueryService implements IOrderQueryService {
    @Override
    public Order FindOrder(String orderId) {
        return null;
    }

    @Override
    public String LocateOrder(String email, String accessCode) {
        return null;
    }

    @Override
    public OrderSeatAssignment[] FindOrderSeatAssignments(String orderId) {
        return new OrderSeatAssignment[0];
    }
//    public Order FindOrder(String orderId) {
//        using(var connection = GetConnection())
//        {
//            var order = connection.QueryList < Order > (new {
//            OrderId = orderId
//        },ConfigSettings.OrderTable).FirstOrDefault();
//            if (order != null) {
//                order.SetLines(connection.QueryList < OrderLine > (new {
//                    OrderId = orderId
//                },ConfigSettings.OrderLineTable).ToList());
//                return order;
//            }
//            return null;
//        }
//    }
//
//    public String?
//
//    LocateOrder(String email, String accessCode) {
//        using(var connection = GetConnection())
//        {
//            var order = connection.QueryList < Order > (new {
//            RegistrantEmail = email, AccessCode = accessCode
//        },ConfigSettings.OrderTable).FirstOrDefault();
//            if (order != null) {
//                return order.OrderId;
//            }
//            return null;
//        }
//    }
//
//    public OrderSeatAssignment[] FindOrderSeatAssignments(String orderId) {
//        using(var connection = GetConnection())
//        {
//            return connection.QueryList < OrderSeatAssignment > (new {
//            OrderId = orderId
//        },ConfigSettings.OrderSeatAssignmentsTable).ToArray();
//        }
//    }
//
//    private IDbConnection GetConnection() {
//        return new SqlConnection(ConfigSettings.ConferenceConnectionString);
//    }

}