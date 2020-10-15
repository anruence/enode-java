package com.microsoft.conference.registration.readmodel.service;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Payment {
    private String id;
    private String orderId;
    private String conferenceId;
    private int state;
    private int totalAmount;
    private String description;
}
