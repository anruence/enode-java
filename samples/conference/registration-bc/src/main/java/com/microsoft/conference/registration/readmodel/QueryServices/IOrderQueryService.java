package com.microsoft.conference.registration.readmodel.QueryServices;

public interface IOrderQueryService {
    Order FindOrder(String orderId);

    String LocateOrder(String email, String accessCode);

    OrderSeatAssignment[] FindOrderSeatAssignments(String orderId);
}
