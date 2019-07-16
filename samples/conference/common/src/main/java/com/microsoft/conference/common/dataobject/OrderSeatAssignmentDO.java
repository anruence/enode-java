package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName(value = "order_seat_assignment")
public class OrderSeatAssignmentDO {
    private String id;
    private String assignmentId;
    private String orderId;
    private Integer position;
    private String seatTypeId;
    private String seatTypeName;
    private String attendeeFirstName;
    private String attendeeLastName;
    private String attendeeEmail;

    public String getId() {
        return this.id;
    }

    public String getAssignmentId() {
        return this.assignmentId;
    }

    public String getOrderId() {
        return this.orderId;
    }

    public Integer getPosition() {
        return this.position;
    }

    public String getSeatTypeId() {
        return this.seatTypeId;
    }

    public String getSeatTypeName() {
        return this.seatTypeName;
    }

    public String getAttendeeFirstName() {
        return this.attendeeFirstName;
    }

    public String getAttendeeLastName() {
        return this.attendeeLastName;
    }

    public String getAttendeeEmail() {
        return this.attendeeEmail;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public void setSeatTypeId(String seatTypeId) {
        this.seatTypeId = seatTypeId;
    }

    public void setSeatTypeName(String seatTypeName) {
        this.seatTypeName = seatTypeName;
    }

    public void setAttendeeFirstName(String attendeeFirstName) {
        this.attendeeFirstName = attendeeFirstName;
    }

    public void setAttendeeLastName(String attendeeLastName) {
        this.attendeeLastName = attendeeLastName;
    }

    public void setAttendeeEmail(String attendeeEmail) {
        this.attendeeEmail = attendeeEmail;
    }
}
