package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "reservation_item")

public class ReservationItemDO {
    private String id;
    private String conferenceId;
    private String reservationId;
    private String seatTypeId;
    private Integer quantity;

    public String getId() {
        return this.id;
    }

    public String getConferenceId() {
        return this.conferenceId;
    }

    public String getReservationId() {
        return this.reservationId;
    }

    public String getSeatTypeId() {
        return this.seatTypeId;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setConferenceId(String conferenceId) {
        this.conferenceId = conferenceId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public void setSeatTypeId(String seatTypeId) {
        this.seatTypeId = seatTypeId;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
