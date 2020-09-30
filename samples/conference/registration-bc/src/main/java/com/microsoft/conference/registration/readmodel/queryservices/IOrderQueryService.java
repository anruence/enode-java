package com.microsoft.conference.registration.readmodel.queryservices;

public interface IOrderQueryService {
    Order findOrder(String orderId);

    String locateOrder(String email, String accessCode);

    OrderSeatAssignment[] findOrderSeatAssignments(String orderId);
}
