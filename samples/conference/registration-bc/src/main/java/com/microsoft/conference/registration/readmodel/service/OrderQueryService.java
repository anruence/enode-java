package com.microsoft.conference.registration.readmodel.service;

import java.util.List;

public interface OrderQueryService {
    OrderVO findOrder(String orderId);

    String locateOrder(String email, String accessCode);

    List<OrderSeatAssignmentVO> findOrderSeatAssignments(String orderId);
}
