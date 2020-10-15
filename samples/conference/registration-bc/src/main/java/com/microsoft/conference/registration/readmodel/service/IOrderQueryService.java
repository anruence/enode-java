package com.microsoft.conference.registration.readmodel.service;

import java.util.List;

public interface IOrderQueryService {
    Order findOrder(String orderId);

    String locateOrder(String email, String accessCode);

    List<OrderSeatAssignment> findOrderSeatAssignments(String orderId);
}
