package com.microsoft.conference.registration.readmodel.service;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class Order {
    private String orderId;
    private String conferenceId;
    private int status;
    private String registrantEmail;
    private String accessCode;
    private BigDecimal totalAmount;
    private Date reservationExpirationDate;
    private List<OrderLine> orderLines = new ArrayList<>();

    public boolean isFreeOfCharge() {
        return totalAmount.equals(BigDecimal.ZERO);
    }
}