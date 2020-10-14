package com.microsoft.conference.registration.readmodel.queryservices;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    public String orderId;
    public String conferenceId;
    public int status;
    public String registrantEmail;
    public String accessCode;
    public BigDecimal totalAmount;
    public Date reservationExpirationDate;
    private List<OrderLine> orderLines = new ArrayList<>();

    public boolean IsFreeOfCharge() {
        return totalAmount.equals(BigDecimal.ZERO);
    }

    public void SetLines(List<OrderLine> lines) {
        orderLines = lines;
    }

    public List<OrderLine> GetLines() {
        return orderLines;
    }
}