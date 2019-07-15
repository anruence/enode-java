package com.microsoft.conference.management.domain.Models;

public class SeatType {
    public String Id;
    public SeatTypeInfo Info;
    public int Quantity;

    public SeatType(String id, SeatTypeInfo info) {
        Id = id;
        Info = info;
    }
}
