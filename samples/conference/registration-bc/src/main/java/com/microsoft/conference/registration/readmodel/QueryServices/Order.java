package com.microsoft.conference.registration.readmodel.QueryServices;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order {
    public String OrderId;
    public String ConferenceId;
    public int Status;
    public String RegistrantEmail;
    public String AccessCode;
    public BigDecimal TotalAmount;
    public Date ReservationExpirationDate;
    private List<OrderLine> _lines = new ArrayList<>();

    public boolean IsFreeOfCharge() {
        return TotalAmount.equals(BigDecimal.ZERO);
    }

    public void SetLines(List<OrderLine> lines) {
        _lines = lines;
    }

    public List<OrderLine> GetLines() {
        return _lines;
    }
}

class OrderLine {
    public String OrderId;
    public String SeatTypeId;
    public String SeatTypeName;
    public int Quantity;
    public BigDecimal UnitPrice;
    public BigDecimal LineTotal;

}
