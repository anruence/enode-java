package com.microsoft.conference.registration.controller;

import com.microsoft.conference.registration.readmodel.service.OrderLineVO;
import com.microsoft.conference.registration.readmodel.service.SeatTypeVO;
import lombok.Data;

@Data
public class OrderItemViewModel {
    private SeatTypeVO SeatType;
    private OrderLineVO OrderLine;
    private int Quantity;
    private int MaxSelectionQuantity;
}