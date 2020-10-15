package com.microsoft.conference.registration.domain.order;

import com.microsoft.conference.registration.domain.SeatQuantity;
import com.microsoft.conference.registration.domain.order.model.OrderTotal;

import java.util.List;

public interface PricingService {
    OrderTotal calculateTotal(String conferenceId, List<SeatQuantity> seats);
}
