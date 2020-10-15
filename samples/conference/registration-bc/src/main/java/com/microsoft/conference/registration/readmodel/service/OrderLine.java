package com.microsoft.conference.registration.readmodel.service;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderLine {
    private String orderId;
    private String seatTypeId;
    private String seatTypeName;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
}
