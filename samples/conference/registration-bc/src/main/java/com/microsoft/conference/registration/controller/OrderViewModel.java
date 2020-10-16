package com.microsoft.conference.registration.controller;

import lombok.Data;

import java.util.List;

@Data
public class OrderViewModel {
    private String OrderId;
    private int OrderVersion;
    private String ConferenceId;
    private String ConferenceCode;
    private String ConferenceName;
    private List<OrderItemViewModel> Items;
    private List<SeatQuantity> Seats;
    private long ReservationExpirationDate;
}
