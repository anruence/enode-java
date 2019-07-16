package com.microsoft.conference.registration.domain.Orders;

import com.microsoft.conference.registration.domain.Orders.Models.OrderTotal;
import com.microsoft.conference.registration.domain.SeatQuantity;

import java.util.List;

public interface IPricingService {
    OrderTotal CalculateTotal(String conferenceId, List<SeatQuantity> seats);
}

