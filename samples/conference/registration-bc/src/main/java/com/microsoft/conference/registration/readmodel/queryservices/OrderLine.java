package com.microsoft.conference.registration.readmodel.queryservices;

import java.math.BigDecimal;

public class OrderLine {
    public String orderId;
    public String seatTypeId;
    public String seatTypeName;
    public int quantity;
    public BigDecimal unitPrice;
    public BigDecimal lineTotal;
}
