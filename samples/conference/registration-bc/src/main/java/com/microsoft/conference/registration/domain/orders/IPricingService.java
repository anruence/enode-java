package com.microsoft.conference.registration.domain.orders;

import com.microsoft.conference.registration.domain.SeatQuantity;
import com.microsoft.conference.registration.domain.orders.models.OrderTotal;

import java.util.List;

public interface IPricingService {
    OrderTotal calculateTotal(String conferenceId, List<SeatQuantity> seats);
}
