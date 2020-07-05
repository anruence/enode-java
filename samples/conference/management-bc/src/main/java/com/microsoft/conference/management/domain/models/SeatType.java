package com.microsoft.conference.management.domain.models;

public class SeatType {
    public String Id;
    public SeatTypeInfo Info;
    public int Quantity;

    public SeatType(String id, SeatTypeInfo info) {
        Id = id;
        Info = info;
    }
}
